package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.project.FileContentResponse;
import com.ansari.projects.lovable_clone.dto.project.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
