package com.ansari.projects.lovable_clone.llm.tools;

import com.ansari.projects.lovable_clone.services.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTools {

    private final ProjectFileService projectFileService;
    private final Long projectId;

//    @Tool(name = "read_files",
//            description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
//    public List<String> readFiles(
//            @ToolParam(description = "List of relative paths (e.g. ['src/App.jsx'])")
//            List<String> paths){
//
//        List<String> result = new ArrayList<>();
//
//        for(String path : paths){
//            String cleanPath = path.startsWith("/") ? path.substring(1) : path;
//
//            String content = projectFileService.getFileContent(projectId,cleanPath).content();
//            result.add(
//                    String.format(
//                            "--- START FILE: %s ---%n%s%n--- END FILE ---%n",
//                            cleanPath, content
//                    )
//            );
//        }
//        return result;
//    }

    @Tool(
            name = "read_files",
            description = "Read the content of files from the FILE_TREE"
    )
    public Mono<List<String>> readFiles(List<String> paths) {

        return Flux.fromIterable(paths)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .map(path -> {

                    String cleanPath = path.startsWith("/") ? path.substring(1) : path;

                    String content = projectFileService
                            .getFileContent(projectId, cleanPath)
                            .content();

                    return String.format(
                            "--- START FILE: %s ---%n%s%n--- END FILE ---%n",
                            cleanPath,
                            content
                    );
                })
                .sequential()
                .collectList();
    }
}
