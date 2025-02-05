package com.resttdd;

import static org.assertj.core.api.Assertions.*;
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

import com.resttdd.domain.member.member.controller.ApiV1MemberController;
import com.resttdd.domain.member.member.entity.Member;
import com.resttdd.domain.member.member.service.MemberService;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // 내부적으로 네트워크를 사용하지 않고, 네트워크를 사용하는 것처럼 보이게 테스트를 진행한다
public class Ap1V1MemberControllerTest {
	@Autowired
	private MockMvc mvc; // 컨트롤러를 테스트하기 위해 사용한다. MockMvc로 내부적으로 서버처럼 보이게 사용할 수 있다

	@Autowired
	private MemberService memberService;

	@Test
	@DisplayName("회원 가입을 할 수 있다")
	void join() throws Exception {
		ResultActions resultActions = mvc // resultActions: 수행하고 난 결과
			.perform(
				post("/api/v1/members/join") // post, get, ...
					.content("""
						{
							"username" : "usernew",
							"password" : "1234",
							"nickname" : "무명"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		Member member = memberService.findByUsername("usernew").get();
		assertThat(member.getNickname()).isEqualTo("무명");

		resultActions
			.andExpect(status().isCreated()) // Expected: 201 CREATED
			.andExpect(handler().handlerType(
				ApiV1MemberController.class)) // Endpoint를 처리하는 Controller: ApiV1MemberController.class
			.andExpect(handler().methodName("join")) // Endpoint를 처리하는 메서드명: "join"
			.andExpect(jsonPath("$.code").value("201-1")) // 결과 body의 JSON 데이터를 검증
			.andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다."))
			.andExpect(jsonPath("$.data").exists()) // JSON에 data가 존재하는지 검증
			.andExpect(jsonPath("$.data.id").isNumber()) // data 내부 id가 숫자인지 검증
			.andExpect(jsonPath("$.data.nickname").value("무명"))
			.andExpect(jsonPath("$.data.createdDate").exists())
			.andExpect(jsonPath("$.data.modifiedDate").exists());
	}

	@Test
	@DisplayName("이미 존재하는 username으로 회원 가입을 하면 실패한다")
	void join2() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/members/join")
					.content("""
						{
							"username" : "user1",
							"password" : "1234",
							"nickname" : "무명"
						}
						""".stripIndent())
					.contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isConflict())
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("join"))
			.andExpect(jsonPath("$.code").value("409-1"))
			.andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."));
	}

	@Test
	@DisplayName("로그인을 할 수 있다")
	void login() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/members/login")
					.content("""
						{
						    "username": "user1",
						    "password": "1234"
						}
						""".stripIndent())
					.contentType(
						new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
					)
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(handler().handlerType(ApiV1MemberController.class))
			.andExpect(handler().methodName("login"))
			.andExpect(jsonPath("$.code").value("200-1"))
			.andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted("유저1")))
			.andExpect(jsonPath("$.data").exists())
			.andExpect(jsonPath("$.data.id").isNumber())
			.andExpect(jsonPath("$.data.nickname").value("유저1"))
			.andExpect(jsonPath("$.data.createdDate").exists())
			.andExpect(jsonPath("$.data.modifiedDate").exists());
	}
}
