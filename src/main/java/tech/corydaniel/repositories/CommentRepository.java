package tech.corydaniel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.corydaniel.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
