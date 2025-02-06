package com.resttdd.domain.post.post.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.resttdd.domain.post.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageDto {
	private List<PostDto> items;
	private int currentPageNo;
	private int totalPages;
	private long totalItems;
	private int pageSize;

	public PageDto(Page<Post> postPage) {
		this.items = postPage.getContent().stream().map(PostDto::new).toList();
		this.currentPageNo = postPage.getNumber();
		this.totalPages = postPage.getTotalPages();
		this.totalItems = postPage.getTotalElements();
		this.pageSize = postPage.getSize();
	}
}
