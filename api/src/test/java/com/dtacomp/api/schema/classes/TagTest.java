package com.dtacomp.api.schema.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TagTest {
    @Test 
    public void deveContruirTagComBuilder() {
        Tag tag = Tag.builder().nome("Teste").build();

        assertEquals("Teste", tag.getNome());
    }
}
