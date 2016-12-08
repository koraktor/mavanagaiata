/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Sebastian Staudt
 */
public class AbstractGitRepositoryTest {

    static GitCommit headCommit = mock(GitCommit.class);

    class GenericGitRepository extends AbstractGitRepository {

        public void check() throws GitRepositoryException {}

        public void close() {}

        public GitTagDescription describe() throws GitRepositoryException {
            return null;
        }

        public String getAbbreviatedCommitId(GitCommit commit)
                throws GitRepositoryException {
            return commit == headCommit ? "deadbeef" : null;
        }

        public String getBranch() throws GitRepositoryException {
            return null;
        }

        public GitCommit getHeadCommit() throws GitRepositoryException {
            return headCommit;
        }

        public File getWorkTree() {
            return new File("test");
        }

        public Map<String, GitTag> getTags() throws GitRepositoryException {
            return null;
        }

        public boolean isDirty(boolean dirtyCheckLoose) throws GitRepositoryException {
            return false;
        }

        @Override
        public boolean isOnUnbornBranch() throws GitRepositoryException {
            return false;
        }

        public <T extends CommitWalkAction> T walkCommits(T action)
                throws GitRepositoryException {
            return action;
        }

    }

    @Test
    public void testGetAbbreviatedHead() throws Exception {
        GitRepository repo = new GenericGitRepository();

        assertThat(repo.getAbbreviatedCommitId(), is(equalTo("deadbeef")));
    }

    @Test
    public void testGetMailMap() throws Exception {
        GitRepository repo = new GenericGitRepository();
        MailMap mailMap = repo.getMailMap();

        assertThat(mailMap.repository, is(equalTo(repo)));
        assertThat(mailMap.mailToMailMap, is(notNullValue()));
        assertThat(mailMap.mailToNameAndMailMap, is(notNullValue()));
        assertThat(mailMap.mailToNameMap, is(notNullValue()));
        assertThat(mailMap.nameAndMailToNameAndMailMap, is(notNullValue()));
    }

    @Test
    public void testGetMailMapAlreadyParsed() throws Exception {
        AbstractGitRepository repo = new GenericGitRepository();
        repo.mailMap = mock(MailMap.class);

        verifyNoMoreInteractions(repo.mailMap);
    }

    @Test
    public void testSetHeadRef() throws Exception {
        AbstractGitRepository repo = new GenericGitRepository();
        repo.setHeadRef("HEAD");

        assertThat(repo.headRef, is(equalTo("HEAD")));
    }
}
