package com.deliverable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deliverable.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
