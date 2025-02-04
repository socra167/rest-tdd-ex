package com.resttdd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // 내부적으로 네트워크를 사용하지 않고, 네트워크를 사용하는 것처럼 보이게 테스트를 진행한다
class RestTddApplicationTests {

	@Test
	void contextLoads() {
	}

}
