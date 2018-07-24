/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2017, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevTag;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JGitTagTest {

    @Test
    public void testNewInstance() throws Exception {
        RevTag rawTag = RevTag.parse(("object 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
                "type commit\n" +
                "tag 1.0.0\n").getBytes());

        JGitTag tag = new JGitTag(rawTag);
        JGitTag tag2 = new JGitTag(rawTag);

        assertThat(tag.getName(), is(equalTo("1.0.0")));
        assertThat(tag, is(equalTo(tag2)));
    }

}
