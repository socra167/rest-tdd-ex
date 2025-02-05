package com.resttdd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.resttdd.domain.post.post.controller.ApiV1PostController;
import com.resttdd.domain.post.post.entity.Post;
import com.resttdd.domain.post.post.service.PostService;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiV1PostControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private PostService postService;

	@Test
	@DisplayName("글 단건 조회를 할 수 있다")
	void item() throws Exception {
		long postId = 1L;
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/posts/1".formatted(postId))
			)
			.andDo(print());

		Post post = postService.getItem(postId).get();

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(ApiV1PostController.class))
			.andExpect(handler().methodName("getItem"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));
		checkPost(resultActions, post);
	}

	private void checkPost(ResultActions resultActions, Post post) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(post.getId()))
			.andExpect(jsonPath("$.data.title").value(post.getTitle()))
			.andExpect(jsonPath("$.data.content").value(post.getContent()))
			.andExpect(jsonPath("$.data.authorid").value(post.getAuthor().getId()))
			.andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
			.andExpect(jsonPath("$.data.createdDate").value(post.getCreatedDate().toString()))
			.andExpect(jsonPath("$.data.modifiedDate").value(post.getModifiedDate().toString()));

	}
}
