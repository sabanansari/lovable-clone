package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEventRepository extends JpaRepository<ChatEvent, Long> {
}
