package com.resttdd.domain.post.comment.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.resttdd.domain.post.comment.entity.Comment;
import com.resttdd.domain.post.post.service.PostService;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiV1CommentControllerTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private PostService postService;

	@Test
	@DisplayName("댓글을 작성할 수 있다")
	void write() throws Exception {
		var apiKey = "user1";
		var content = "댓글의 내용입니다.";
		var postId = 1L;
		var resultActions = mvc
			.perform(
				post("/api/v1/posts/%d/comments".formatted(postId))
					.header("Authorization", "Bearer " + apiKey)
					.content("""
						{
							"content" : "%s",
						}
						""".formatted(content).trim().stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isCreated())
			.andExpect(handler().handlerType(ApiV1CommentController.class))
			.andExpect(handler().methodName("write"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.msg").value("댓글 작성이 완료되었습니다."));
		var post = postService.getItem(postId).get();
		var comment = post.getLatestComment();
		checkComment(resultActions, comment);
	}

	private void checkComment(ResultActions resultActions, Comment comment) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(comment.getId()))
			.andExpect(jsonPath("$.data.content").value(comment.getContent()))
			.andExpect(jsonPath("$.data.authorId").value(comment.getAuthor().getId()))
			.andExpect(jsonPath("$.data.authorName").value(comment.getAuthor().getNickname()))
			.andExpect(jsonPath("$.data.createdDate").value(
				matchesPattern(comment.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.modifiedDate").value(
				matchesPattern(comment.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
	}
}