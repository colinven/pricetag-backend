package com.pricetag.backend.email;

public record RenderedEmail(
        String html,
        String text
) {
}
