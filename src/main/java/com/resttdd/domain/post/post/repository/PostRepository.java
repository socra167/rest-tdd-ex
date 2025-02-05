package com.resttdd.domain.post.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resttdd.domain.post.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	Optional<Post> findTopByOrderByIdDesc();
}
