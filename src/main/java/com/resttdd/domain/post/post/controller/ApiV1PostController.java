package com.resttdd.domain.post.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resttdd.domain.member.member.entity.Member;
import com.resttdd.domain.post.post.dto.PageDto;
import com.resttdd.domain.post.post.dto.PostDto;
import com.resttdd.domain.post.post.entity.Post;
import com.resttdd.domain.post.post.service.PostService;
import com.resttdd.global.Rq;
import com.resttdd.global.dto.RsData;
import com.resttdd.global.exception.ServiceException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
	private final PostService postService;
	private final Rq rq;

	@GetMapping
	public RsData<PageDto> getItems(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "3") int pageSize) {
		Page<Post> postPage = postService.getListedItems(page, pageSize);

		return new RsData<>(
			"200-1",
			"글 목록 조회가 완료되었습니다.",
			new PageDto(postPage)
		);
	}

	@GetMapping("{id}")
	public RsData<PostDto> getItem(@PathVariable long id) {
		Post post = postService.getItem(id)
			.orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

		if (!post.isPublished()) {
			Member actor = rq.getAuthenticatedActor();
			post.canRead(actor);
		}

		return new RsData<>(
			"200-1",
			"%d번 글을 조회하였습니다.".formatted(post.getId()),
			new PostDto(post)
		);
	}

	record WriteReqBody(@NotBlank String title, @NotBlank String content, boolean published, boolean listed) {
	}

	@PostMapping
	public RsData<PostDto> write(@RequestBody @Valid WriteReqBody body) {
		Member actor = rq.getAuthenticatedActor();

		Post post = postService.write(actor, body.title(), body.content(), body.published(), body.listed());

		return new RsData<>(
			"201-1",
			"%d번 글 작성이 완료되었습니다.".formatted(post.getId()),
			new PostDto(post)
		);
	}

	@PutMapping("{id}")
	public RsData<PostDto> modify(@PathVariable long id, @RequestBody @Valid WriteReqBody body) {
		Member actor = rq.getAuthenticatedActor();
		Post post = postService.getItem(id)
			.orElseThrow(() -> new ServiceException("404-1", "존재하지 않는 글입니다."));

		postService.modify(post, body.title(), body.content());

		post.canModify(actor);

		return new RsData<>(
			"200-1",
			"%d번 글 수정이 완료되었습니다.".formatted(post.getId()),
			new PostDto(post)
		);
	}

	@DeleteMapping("{id}")
	public RsData<Void> delete(@PathVariable long id) {
		Member actor = rq.getAuthenticatedActor();
		Post post = postService.getItem(id).get();

		post.canDelete(actor);

		postService.delete(post);

		return new RsData<>(
			"200-1",
			"%d번 글 삭제가 완료되었습니다.".formatted(post.getId())
		);
	}
}
