package com.dtacomp.dto.tag;

import java.util.UUID;

import com.dtacomp.api.schema.classes.Tag;

public record TagResponse(
    UUID id,
    String nome
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getNome());
    }
}
