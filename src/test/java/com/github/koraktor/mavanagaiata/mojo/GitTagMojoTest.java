/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitTagDescription;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitTagMojoTest extends MojoAbstractTest<GitTagMojo> {

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();

        GitTagDescription description = mock(GitTagDescription.class);
        when(description.getNextTagName()).thenReturn("2.0.0");
        when(description.toString()).thenReturn("2.0.0-2-gdeadbeef");
        when(repository.describe()).thenReturn(description);
    }

    @Test
    public void testError() {
        super.testError("Unable to read Git tag");
    }

    @Test
    public void testResult() throws Exception {
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testCustomDirtyFlag() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "*";
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef*", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testDirty() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef-dirty", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testDisabledDirtyFlag() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "null";
        mojo.prepareParameters();
        mojo.run(repository);

        assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        assertProperty("2.0.0", "tag.name");
    }

}
