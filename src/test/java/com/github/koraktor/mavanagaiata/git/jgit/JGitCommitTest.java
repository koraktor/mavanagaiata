/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2026, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jgit.revwalk.RevCommit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

@DisplayName("JGitCommit")
class JGitCommitTest {

    @DisplayName("can be created from JGit’s RevCommit objects")
    @Test
    void test() {
        RevCommit rawCommit = RevCommit.parse(("tree 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
                "author John Doe <john.doe@example.com> 1162580880 +0000\n" +
                "committer Sebastian Staudt <koraktor@gmail.com> 1275131880 +0200\n" +
                "\n" +
                "Commit subject\n\nFull message.").getBytes());
        Instant authorInstant = Instant.ofEpochMilli(1162580880000L);
        Instant committerInstant = Instant.ofEpochMilli(1275131880000L);

        JGitCommit commit = new JGitCommit(rawCommit);
        JGitCommit commit2 = new JGitCommit(rawCommit);

        assertThat(commit.getAuthorInstant(), is(equalTo(authorInstant)));
        assertThat(commit.getAuthorEmailAddress(), is(equalTo("john.doe@example.com")));
        assertThat(commit.getAuthorName(), is(equalTo("John Doe")));
        assertThat(commit.getAuthorTimeZoneId(), is(ZoneId.of("Z")));
        assertThat(commit.getCommitterInstant(), is(equalTo(committerInstant)));
        assertThat(commit.getCommitterEmailAddress(), is(equalTo("koraktor@gmail.com")));
        assertThat(commit.getCommitterName(), is(equalTo("Sebastian Staudt")));
        assertThat(commit.getCommitterTimeZoneId(), is(ZoneId.of("+0200")));
        assertThat(commit.getId(), is(equalTo("518a7e6955ee38ecab34254e0796860bc679cfee")));
        assertThat(commit.getMessage(), is(equalTo("Commit subject\n\nFull message.")));
        assertThat(commit.getMessageSubject(), is(equalTo("Commit subject")));
        assertThat(commit.hashCode(), is(equalTo(rawCommit.getId().hashCode())));
        assertThat(commit.isMergeCommit(), is(false));
        assertThat(commit, is(equalTo(commit2)));
        assertThat(commit, is(not(equalTo(authorInstant))));
    }

    @DisplayName("should recognize merge commits")
    @Test
    void testMergeCommit() {
        RevCommit rawCommit = RevCommit.parse(("tree 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
            "parent 06cee865ab7f006a58be39f1d46f01dcb1880105\n" +
            "parent afb48c6be4278ba7f5e4197b80adbbb80c6df3a7\n" +
            "author John Doe <john.doe@example.com> 1162580880 +0000\n" +
            "committer Sebastian Staudt <koraktor@gmail.com> 1275131880 +0200\n" +
            "\n" +
            "Commit subject\n\nFull message.").getBytes());

        JGitCommit commit = new JGitCommit(rawCommit);

        assertThat(commit.isMergeCommit(), is(true));
    }

}
