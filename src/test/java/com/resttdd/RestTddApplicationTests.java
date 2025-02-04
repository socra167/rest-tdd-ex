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

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // 내부적으로 네트워크를 사용하지 않고, 네트워크를 사용하는 것처럼 보이게 테스트를 진행한다
class RestTddApplicationTests {

	@Autowired
	private MockMvc mvc; // 컨트롤러를 테스트하기 위해 사용한다. MockMvc로 내부적으로 서버처럼 보이게 사용할 수 있다

	@Test
	@DisplayName("회원 가입을 할 수 있다")
	void join() throws Exception {
		ResultActions resultActions = mvc // resultActions: 수행하고 난 결과
			.perform(
				post("/api/v1/members/join") // post, get, ...
			)
			.andDo(print());

		resultActions
			.andExpect(status().isCreated()); // Expected: 201 CREATED
		// 엔드포인트를 처리하는 컨트롤러가 없는 상태이므로 당연히 404가 응답되고, 테스트가 실패한다
	}
}
