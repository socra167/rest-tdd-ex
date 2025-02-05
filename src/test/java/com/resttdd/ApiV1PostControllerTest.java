package com.resttdd;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

	@Nested
	@DisplayName("글 단건 조회")
	class getItem {

		@Test
		@DisplayName("성공 - 글 단건 조회를 할 수 있다")
		void itemA() throws Exception {
			var postId = 1L;
			var resultActions = itemRequest(postId);
			var post = postService.getItem(postId).get();

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItem"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));
			checkPost(resultActions, post);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 글을 조회하면 실패한다")
		void itemB() throws Exception {
			var postId = 9999999L;
			var resultActions = itemRequest(postId);

			resultActions
				.andExpect(status().isNotFound())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("getItem"))
				.andExpect(jsonPath("$.code").value("404-1"))
				.andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
		}

		private ResultActions itemRequest(long postId) throws Exception {
			return mvc
				.perform(
					get("/api/v1/posts/%s".formatted(postId))
				)
				.andDo(print());
		}

	}

	private void checkPost(ResultActions resultActions, Post post) throws Exception {
		resultActions
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").value(post.getId()))
			.andExpect(jsonPath("$.data.title").value(post.getTitle()))
			.andExpect(jsonPath("$.data.content").value(post.getContent()))
			.andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
			.andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
			.andExpect(jsonPath("$.data.createdDate").value(containsString(post.getCreatedDate().toString())))
			.andExpect(jsonPath("$.data.modifiedDate").value(containsString(post.getModifiedDate().toString())));
	}

	@Test
	@DisplayName("글을 작성할 수 있다")
	void writeA() throws Exception {
		var apiKey = "user1";
		var title = "새로운 글 제목";
		var content = "새로운 글 내용";
		var resultActions = writeRequest(apiKey, title, content);
		var post = postService.getLatestItem().get();

		resultActions
			.andExpect(status().isCreated())
			.andExpect(handler().handlerType(ApiV1PostController.class))
			.andExpect(handler().methodName("write"))
			.andExpect(jsonPath("$.code").value("201-1"))
			.andExpect(jsonPath("$.msg").value("%d번 글 작성이 완료되었습니다.".formatted(post.getId())));
		checkPost(resultActions, post);
	}

	private ResultActions writeRequest(String apiKey, String title, String content) throws Exception {
		return mvc
			.perform(
				post("/api/v1/posts")
					.header("Authorization", "Bearer %s".formatted(apiKey))
					.content("""
							{
								"title" : "%s",
								"content" : "%s"
							}
							""".formatted(title, content).stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());
	}
}
