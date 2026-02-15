package com.ansari.projects.lovable_clone.controllers;

import com.ansari.projects.lovable_clone.dto.project.FileContentResponse;
import com.ansari.projects.lovable_clone.dto.project.FileNode;
import com.ansari.projects.lovable_clone.services.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
public class FileController {

    private final ProjectFileService fileService;

    @GetMapping
    public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId){
        Long userId = 1L;
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/{*path}") // src/hooks/AppHook.jsx - from *
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @PathVariable String path) {
        Long userId = 1L;
        return ResponseEntity.ok(fileService.getFileContent(projectId,path));
    }

}
