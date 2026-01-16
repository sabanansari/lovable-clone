package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
