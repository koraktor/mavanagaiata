/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.util.Map;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 *
 *
 * @author Sebastian Staudt
 */
public class AbstractGitRepositoryTest {

    class GenericGitRepository extends AbstractGitRepository {

        public void check() throws GitRepositoryException {}

        public void close() {}

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

        public Map<String, GitTag> getTags() throws GitRepositoryException {
            return null;
        }

        public boolean isDirty() throws GitRepositoryException {
            return false;
        }

        public void walkCommits(CommitWalkAction action)
                throws GitRepositoryException {}

    }

    @Test
    public void testFinalize() throws Exception {
        AbstractGitRepository repo = spy(new GenericGitRepository());

        repo.finalize();

        verify(repo).close();
    }

    @Test
    public void testSetHeadRef() throws Exception {
        AbstractGitRepository repo = new GenericGitRepository();
        repo.setHeadRef("HEAD");

        assertThat(repo.headRef, is(equalTo("HEAD")));
    }
}
