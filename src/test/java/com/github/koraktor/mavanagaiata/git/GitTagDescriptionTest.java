/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitTagDescriptionTest {

    private GitTag tag;

    @Before
    public void setup() {
        tag = mock(GitTag.class);
        when(tag.getName()).thenReturn("1.0.0");
    }

    @Test
    public void test() {
        GitTagDescription description = new GitTagDescription("deadbeef", tag, 3);

        assertThat(description.getNextTagName(), is(equalTo("1.0.0")));
        assertThat(description.isTagged(), is(false));
        assertThat(description.toString(), is(equalTo("1.0.0-3-gdeadbeef")));
    }

    @Test
    public void testNoTag() {
        GitTagDescription description = new GitTagDescription("deadbeef", null, -1);

        assertThat(description.getNextTagName(), is(equalTo("")));
        assertThat(description.isTagged(), is(false));
        assertThat(description.toString(), is(equalTo("deadbeef")));
    }

    @Test
    public void testTagged() {
        GitTagDescription description = new GitTagDescription("deadbeef", tag, 0);

        assertThat(description.getNextTagName(), is(equalTo("1.0.0")));
        assertThat(description.isTagged(), is(true));
        assertThat(description.toString(), is(equalTo("1.0.0")));
    }

}
