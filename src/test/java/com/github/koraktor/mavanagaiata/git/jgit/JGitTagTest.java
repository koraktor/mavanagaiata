/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevTag;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class JGitTagTest {

    @Test
    public void test() throws Exception {
        RevTag rawTag = RevTag.parse(("object 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
                "type commit\n" +
                "tag 1.0.0\n" +
                "tagger Sebastian Staudt <koraktor@gmail.com> 1275131880 +0200\n" +
                "\n" +
                "Version 1.0.0\n").getBytes());
        Date tagDate = new Date(1275131880000L);

        JGitTag tag = new JGitTag(rawTag);
        JGitTag tag2 = new JGitTag(rawTag);

        assertThat(tag.getDate(), is(equalTo(tagDate)));
        assertThat(tag.getName(), is(equalTo("1.0.0")));
        assertThat(tag.getTimeZone(), is(TimeZone.getTimeZone("GMT+0200")));
        assertThat(tag, is(equalTo(tag2)));
    }

}
