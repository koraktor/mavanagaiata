/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2025, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Sebastian Staudt
 */
@DisplayName("AbstractGitRepository")
class AbstractGitRepositoryTest {

    private final static GitCommit headCommit = mock(GitCommit.class);

    static class GenericGitRepository extends AbstractGitRepository {

        public void check() {}

        public void close() {}

        public GitTagDescription describe() {
            return null;
        }

        public String getAbbreviatedCommitId(GitCommit commit) {
            return commit == headCommit ? "deadbeef" : null;
        }

        public String getBranch() {
            return null;
        }

        public GitCommit getHeadCommit() {
            return headCommit;
        }

        public String getHeadRef() {
            return null;
        }

        public File getWorkTree() {
            return new File("test");
        }

        public Map<String, GitTag> getTags() {
            return null;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        public boolean isDirty(boolean dirtyCheckLoose) {
            return false;
        }

        @Override
        public boolean isOnUnbornBranch() {
            return false;
        }

        @Override
        public void loadTag(GitTag tag) {}

        public void  walkCommits(CommitWalkAction action) {}

    }

    @DisplayName("should be able to abbreviate the HEAD commit")
    @Test
    void testGetAbbreviatedHead() throws Exception {
        try (GitRepository repo = new GenericGitRepository()) {
            assertThat(repo.getAbbreviatedCommitId(), is(equalTo("deadbeef")));
        }
    }

    @DisplayName("should be able to get the mailmap")
    @Test
    void testGetMailMap() throws Exception {
        GitRepository repo = new GenericGitRepository();
        MailMap mailMap = repo.getMailMap();

        assertThat(mailMap.repository, is(equalTo(repo)));
        assertThat(mailMap.mailToMailMap, is(notNullValue()));
        assertThat(mailMap.mailToNameAndMailMap, is(notNullValue()));
        assertThat(mailMap.mailToNameMap, is(notNullValue()));
        assertThat(mailMap.nameAndMailToNameAndMailMap, is(notNullValue()));
    }

    @DisplayName("should cache the mailmap")
    @Test
    void testGetMailMapAlreadyParsed() {
        try (AbstractGitRepository repo = new GenericGitRepository()) {
            repo.mailMap = mock(MailMap.class);

            verifyNoMoreInteractions(repo.mailMap);
        }
    }
}
