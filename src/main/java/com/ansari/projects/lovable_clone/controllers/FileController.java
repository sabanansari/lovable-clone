package com.ansari.projects.lovable_clone.controllers;

import com.ansari.projects.lovable_clone.dto.project.FileContentResponse;
import com.ansari.projects.lovable_clone.dto.project.FileTreeResponse;
import com.ansari.projects.lovable_clone.services.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
public class FileController {

    private final ProjectFileService fileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId){

        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/content") // src/hooks/AppHook.jsx - from *
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @RequestParam String path) {

        return ResponseEntity.ok(fileService.getFileContent(projectId,path));
    }

}
