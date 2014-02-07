/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2014, Sebastian Staudt
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
        when(this.repository.describe()).thenReturn(description);
    }

    @Test
    public void testError() {
        super.testError("Unable to read Git tag");
    }

    @Test
    public void testResult() throws Exception {
        this.mojo.run();

        this.assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        this.assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testCustomDirtyFlag() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.dirtyFlag = "*";
        this.mojo.run();

        this.assertProperty("2.0.0-2-gdeadbeef*", "tag.describe");
        this.assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testDirty() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.run();

        this.assertProperty("2.0.0-2-gdeadbeef-dirty", "tag.describe");
        this.assertProperty("2.0.0", "tag.name");
    }

    @Test
    public void testDisabledDirtyFlag() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.dirtyFlag = "null";
        this.mojo.prepareParameters();
        this.mojo.run();

        this.assertProperty("2.0.0-2-gdeadbeef", "tag.describe");
        this.assertProperty("2.0.0", "tag.name");
    }

}
