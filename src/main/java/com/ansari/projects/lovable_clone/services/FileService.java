package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.project.FileContentResponse;
import com.ansari.projects.lovable_clone.dto.project.FileNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FileService {
    List<FileNode> getFileTree(Long projectId, Long userId);

    FileContentResponse getFileContent(Long projectId, String path, Long userId);
}
