package com.resttdd.domain.member.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resttdd.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

	@PostMapping("/join")
	public RsData<Void> join() {
		return new RsData<>("201-1", "회원 가입이 완료되었습니다.");
	}
}
