package com.resttdd.domain.post.post.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resttdd.domain.member.member.entity.Member;
import com.resttdd.domain.post.post.dto.PostDto;
import com.resttdd.domain.post.post.entity.Post;
import com.resttdd.domain.post.post.service.PostService;
import com.resttdd.global.Rq;
import com.resttdd.global.dto.RsData;
import com.resttdd.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
	private final PostService postService;
	private final Rq rq;

	@GetMapping("{id}")
	public RsData<PostDto> getItem(@PathVariable long id) {
		Post post = postService.getItem(id)
			.orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

		return new RsData<>(
			"200-1",
			"%d번 글을 조회하였습니다.".formatted(post.getId()),
			new PostDto(post)
		);
	}

	record WriteReqBody(String title, String content) {
	}

	@PostMapping
	public RsData<PostDto> write(@RequestBody WriteReqBody body) {
		Member actor = rq.getAuthenticatedActor();

		Post post = postService.write(actor, body.title(), body.content());

		return new RsData<>(
			"201-1",
			"%d번 글 작성이 완료되었습니다.".formatted(post.getId()),
			new PostDto(post)
		);
	}
}
