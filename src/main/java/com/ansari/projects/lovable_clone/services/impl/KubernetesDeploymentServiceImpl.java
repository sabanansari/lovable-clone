package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.deploy.DeployResponse;
import com.ansari.projects.lovable_clone.services.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesDeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient client;
    private final StringRedisTemplate redisTemplate;

    private static final String NAMESPACE = "shuttle-apps";
    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";
    private static final String SYNCER_CONTAINER = "syncer";
    private static final String RUNNER_CONTAINER = "runner";
    private static final String REVERSE_PROXY_PORT = "8090";

    @Override
    public DeployResponse deploy(Long projectId) {

        log.info("Received deployment request for project {}", projectId);

        String domain = "project-" + projectId + ".app.domain.com";

        log.info("Checking for existing active pod for project {}", projectId);

        Pod existingPod = findActivePod(projectId).orElse(null);

        log.info("Existing active pod {} for project {}", existingPod != null ? existingPod.getMetadata().getName() : "not found", projectId);

        if(existingPod != null){
            registerRoute(domain, existingPod);
            return new DeployResponse("http://"+domain+":"+REVERSE_PROXY_PORT);
        }

        return claimAndStartNewPod(projectId, domain);
    }


    private DeployResponse claimAndStartNewPod(Long projectId, String domain) {

        log.info("Claiming new pod for project {}", projectId);
        Pod pod = client.pods().inNamespace(NAMESPACE)
                .withLabel(POOL_LABEL, IDLE)
                .list()
                .getItems()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No idle pods available"));

        String podName = pod.getMetadata().getName();
        log.info("Claiming pod {} for project {}", podName, projectId);

        client.pods().inNamespace(NAMESPACE)
                .withName(podName)
                .edit(p -> {
                    p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
                    p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
                    return p;
                });

        try{

            log.info("Starting synchronization for pod {} and project {}", podName, projectId);
            String initialSyncCmd = String.format(
                    "rm -rf /app/* && mc mirror --overwrite myminio/projects/%d/ /app/", projectId
            );

            log.info("Starting initial sync for pod {}: {}", podName, initialSyncCmd);
            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", initialSyncCmd);

            String watchCmd = String.format(
                    "nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &", projectId
            );

            log.info("Starting watch sync for pod {}: {}", podName, watchCmd);

            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", watchCmd);

            //Runner Commands
//String startCmd = "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";

            String startCmd =
                    "nohup sh -c 'npm install && npm run dev -- --host 0.0.0.0 --port 5173' > /app/dev.log 2>&1 &";

            log.info("Starting application for pod {}: {}", podName, startCmd);
            execCommand(podName, RUNNER_CONTAINER, "sh", "-c", startCmd);

            log.info("Deployment completed for project {} on pod {}. Accessible at http://{}:{}", projectId, podName, domain, REVERSE_PROXY_PORT);

            registerRoute(domain, pod);

            return new DeployResponse("http://"+domain+":"+REVERSE_PROXY_PORT);

        }catch(Exception e){
            log.error("Failed to claim pod {} for project {}", podName, projectId, e);
            client.pods().inNamespace(NAMESPACE).withName(podName).delete();
            throw new RuntimeException("Failed to claim pod", e);
        }





    }

    private void registerRoute(String domain, Pod pod){
        String podIp = pod.getStatus().getPodIP();
        if(podIp == null) throw new RuntimeException("Pod is running but has no IP!");

        redisTemplate.opsForValue().set("route:"+domain, podIp+":5173",6,TimeUnit.HOURS);
    }

//    private void registerRoute(String domain, Pod pod){
//
//        String podName = pod.getMetadata().getName();
//
//        if(podName == null){
//            throw new RuntimeException("Pod has no name!");
//        }
//
//        String target =
//                podName + "." +
//                        NAMESPACE +
//                        ".pod.cluster.local:5173";
//
//        redisTemplate.opsForValue().set(
//                "route:" + domain,
//                target,
//                6,
//                TimeUnit.HOURS
//        );
//
//        log.info("Registered route {} -> {}", domain, target);
//    }

    private void execCommand(String podName, String container, String... command){
        log.info("Executing command in pod {}: {}", podName, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();

        try(ExecWatch ignored = client.pods().inNamespace(NAMESPACE)
                .withName(podName)
                .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {
            // Wait for command to complete or timeout
            if(command[command.length - 1].trim().endsWith("&")){
                Thread.sleep(500);
            }else{
                data.get(30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Exec failed",e);
            throw new RuntimeException("Pod Execution failed", e);
        }
    }

    Optional<Pod> findActivePod(Long projectId){

        log.info("Searching for active pod for project {}", projectId);
        return client.pods().inNamespace(NAMESPACE)
                .withLabel(POOL_LABEL, BUSY)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .list()
                .getItems()
                .stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst();
    }
}
