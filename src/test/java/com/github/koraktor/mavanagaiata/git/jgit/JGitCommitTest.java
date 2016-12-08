/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevCommit;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class JGitCommitTest {

    @Test
    public void test() {
        RevCommit rawCommit = RevCommit.parse(("tree 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
                "author John Doe <john.doe@example.com> 1162580880 +0000\n" +
                "committer Sebastian Staudt <koraktor@gmail.com> 1275131880 +0200\n" +
                "\n" +
                "Commit subject\n\nFull message.").getBytes());
        Date authorDate = new Date(1162580880000L);
        Date committerDate = new Date(1275131880000L);

        JGitCommit commit = new JGitCommit(rawCommit);
        JGitCommit commit2 = new JGitCommit(rawCommit);

        assertThat(commit.getAuthorDate(), is(equalTo(authorDate)));
        assertThat(commit.getAuthorEmailAddress(), is(equalTo("john.doe@example.com")));
        assertThat(commit.getAuthorName(), is(equalTo("John Doe")));
        assertThat(commit.getAuthorTimeZone(), is(TimeZone.getTimeZone("GMT+0000")));
        assertThat(commit.getCommitterDate(), is(equalTo(committerDate)));
        assertThat(commit.getCommitterEmailAddress(), is(equalTo("koraktor@gmail.com")));
        assertThat(commit.getCommitterName(), is(equalTo("Sebastian Staudt")));
        assertThat(commit.getCommitterTimeZone(), is(TimeZone.getTimeZone("GMT+0200")));
        assertThat(commit.getId(), is(equalTo("518a7e6955ee38ecab34254e0796860bc679cfee")));
        assertThat(commit.getMessage(), is(equalTo("Commit subject\n\nFull message.")));
        assertThat(commit.getMessageSubject(), is(equalTo("Commit subject")));
        assertThat(commit.hashCode(), is(equalTo(rawCommit.getId().hashCode())));
        assertThat(commit, is(equalTo(commit2)));
        assertThat(commit.equals(authorDate), is(false));
    }

}
