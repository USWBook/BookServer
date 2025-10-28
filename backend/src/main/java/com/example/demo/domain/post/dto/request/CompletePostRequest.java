package com.example.demo.domain.post.dto.request;

import java.util.UUID;

public record CompletePostRequest(
        UUID buyerId
) {
}
