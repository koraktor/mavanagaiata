/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitTagDescription;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
@DisplayName("TagMojo")
class TagMojoTest extends MojoAbstractTest<TagMojo> {

    @BeforeEach
    @Override
    public void setup() throws Exception {
        super.setup();

        GitTagDescription description = mock(GitTagDescription.class);
        when(description.getNextTagName()).thenReturn("2.0.0");
        when(description.toString()).thenReturn("2.0.0-2-gdeadbeef");
        when(repository.describe()).thenReturn(description);
    }

    @DisplayName("should handle errors")
    @Test
    void testError() {
        super.testError("Unable to read Git tag");
    }

    @DisplayName("should provide properties for tag and description")
    @Test
    void testResult() throws Exception {
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @DisplayName("should add custom dirty flags to the description")
    @Test
    void testCustomDirtyFlag() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "*";
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef*", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @DisplayName("should add a flag to the description if the worktree is dirty")
    @Test
    void testDirty() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef-dirty", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @DisplayName("should not add a flag to the description if configured")
    @Test
    void testDisabledDirtyFlag() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "null";
        mojo.prepareParameters();
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

}
