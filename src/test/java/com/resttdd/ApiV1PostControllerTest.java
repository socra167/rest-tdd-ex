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
			.andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
			.andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
	}

	@Nested
	@DisplayName("글 작성")
	class write {

		@Test
		@DisplayName("성공 - 글을 작성할 수 있다")
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

		@Test
		@DisplayName("실패 - 잘못된 API key로 글을 작성하면 실패한다")
		void writeB() throws Exception {
			var apiKey = "";
			var title = "새로운 글 제목";
			var content = "새로운 글 내용";
			var resultActions = writeRequest(apiKey, title, content);

			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("write"))
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
		}

		@Test
		@DisplayName("실패 - 입력 데이터가 누락되면 글 작성에 실패한다")
		void writeC() throws Exception {
			var apiKey = "user1";
			var title = "";
			var content = "";
			var resultActions = writeRequest(apiKey, title, content);

			resultActions
				.andExpect(status().isBadRequest())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("write"))
				.andExpect(jsonPath("$.code").value("400-1"))
				.andExpect(jsonPath("$.msg").value("""
					content : NotBlank : must not be blank
					title : NotBlank : must not be blank
					""".trim().stripIndent()));
		}

	}

	@Nested
	@DisplayName("글 수정")
	class modify {

		@Test
		@DisplayName("성공 - 글을 수정할 수 있다")
		void modifyA() throws Exception {
			var postId = 1L;
			var apiKey = "user1";
			var title = "수정된 글 제목";
			var content = "수정된 글 내용";
			var resultActions = modifyRequest(postId, apiKey, title, content);

			resultActions
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("modify"))
				.andExpect(jsonPath("$.code").value("200-1"))
				.andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)));
			var post = postService.getItem(postId).get();
			checkPost(resultActions, post);
		}

		private ResultActions modifyRequest(Long postId, String apiKey, String title, String content) throws Exception {
			return mvc
				.perform(
					put("/api/v1/posts/%d".formatted(postId))
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

		@Test
		@DisplayName("실패 - 잘못된 API key 글을 수정하면 실패한다")
		void modifyB() throws Exception {
			var postId = 1L;
			var apiKey = "wrong_api_key";
			var title = "수정된 글 제목";
			var content = "수정된 글 내용";
			var resultActions = modifyRequest(postId, apiKey, title, content);

			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(handler().handlerType(ApiV1PostController.class))
				.andExpect(handler().methodName("modify"))
				.andExpect(jsonPath("$.code").value("401-1"))
				.andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
		}
	}
}
