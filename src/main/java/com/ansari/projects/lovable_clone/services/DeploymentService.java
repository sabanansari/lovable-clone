package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.deploy.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
