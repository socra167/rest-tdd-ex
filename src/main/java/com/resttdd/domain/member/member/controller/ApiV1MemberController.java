package com.resttdd.domain.member.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resttdd.domain.member.member.dto.MemberDto;
import com.resttdd.domain.member.member.entity.Member;
import com.resttdd.domain.member.member.service.MemberService;
import com.resttdd.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

	private final MemberService memberService;

	record JoinReqBody(String username, String password, String nickname) {
	}

	@PostMapping("/join")
	public RsData<MemberDto> join(@RequestBody JoinReqBody reqBody) {
		Member member = memberService.join(reqBody.username(), reqBody.password(), reqBody.nickname());
		return new RsData<>(
			"201-1",
			"회원 가입이 완료되었습니다.",
			new MemberDto(member)
		);
	}
}
