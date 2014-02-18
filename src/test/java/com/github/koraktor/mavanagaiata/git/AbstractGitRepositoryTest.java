/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2014, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 *
 *
 * @author Sebastian Staudt
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractGitRepository.class)
public class AbstractGitRepositoryTest {

    public static boolean closed = false;

    class GenericGitRepository extends AbstractGitRepository {

        public void check() throws GitRepositoryException {}

        public void close() {
            closed = true;
        }

        public GitTagDescription describe() throws GitRepositoryException {
            return null;
        }

        public String getAbbreviatedCommitId(GitCommit commit)
                throws GitRepositoryException {
            return null;
        }

        public String getBranch() throws GitRepositoryException {
            return null;
        }

        public GitCommit getHeadCommit() throws GitRepositoryException {
            return null;
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

        public void walkCommits(CommitWalkAction action)
                throws GitRepositoryException {}

    }

    @Test
    public void testFinalize() throws Throwable {
        closed = false;
        AbstractGitRepository repo = new GenericGitRepository();
        repo.finalize();

        assertThat(closed, is(true));
    }

    @Test
    public void testGetMailMap() throws Exception {
        MailMap mailMap = mock(MailMap.class);
        AbstractGitRepository repo = new GenericGitRepository();
        whenNew(MailMap.class).withNoArguments().thenReturn(mailMap);

        assertThat(repo.getMailMap(), is(equalTo(mailMap)));

        verify(mailMap).parseMailMap(repo);
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
