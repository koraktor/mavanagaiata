/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2016-2017, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.apache.maven.plugin.logging.Log;

import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
public class CheckMojoTest extends MojoAbstractTest<CheckMojo> {

    @Test
    public void testChangedHead() throws MavanagaiataMojoException {
        Log log = mock(Log.class);
        mojo.setLog(log);
        mojo.head = "branch";

        mojo.run(repository);

        verify(log).warn("Your configuration specifies `branch` " +
                "(instead of `" + GitRepository.DEFAULT_HEAD + "`) as the " +
                "current commit. The results of these checks might not match " +
                "the actual repository state.");
    }

    @Test
    public void testCheckBranchFailed() throws Exception {
        mojo.checkBranch = "production";

        when(repository.getBranch()).thenReturn("master");

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (CheckMojoException e) {
            assertThat(e.type, is(CheckMojoException.Type.WRONG_BRANCH));
            assertThat(e.isGraceful(), is(true));
        }
    }

    @Test
    public void testCheckBranchSuccess() throws Exception {
        mojo.checkBranch = "production";

        when(repository.getBranch()).thenReturn("production");

        mojo.run(repository);
    }

    @Test
    public void testCheckCleanFailed() throws Exception {
        mojo.checkClean = true;

        when(repository.isDirty(false)).thenReturn(true);

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (CheckMojoException e) {
            assertThat(e.type, is(CheckMojoException.Type.UNCLEAN));
            assertThat(e.isGraceful(), is(true));
        }
    }

    @Test
    public void testCheckCleanSuccess() throws Exception {
        mojo.checkClean = true;

        when(repository.isDirty(false)).thenReturn(false);

        mojo.run(repository);
    }

    @Test
    public void testCheckCommitMessageFailed() throws Exception {
        mojo.checkCommitMessage = "Release";
        mojo.initConfiguration();

        when(repository.getHeadCommit().getMessage()).thenReturn("Fix some bugs");

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (CheckMojoException e) {
            assertThat(e.type, is(CheckMojoException.Type.WRONG_COMMIT_MSG));
            assertThat(e.isGraceful(), is(true));
        }
    }

    @Test
    public void testCheckCommitMessageSuccess() throws Exception {
        mojo.checkCommitMessage = "Release";
        mojo.initConfiguration();

        when(repository.getHeadCommit().getMessage()).thenReturn("Release 1.0.0");

        mojo.run(repository);
    }

    @Test
    public void testCheckTagFailed() throws Exception {
        mojo.checkTag = true;

        when(repository.describe().isTagged()).thenReturn(false);

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (CheckMojoException e) {
            assertThat(e.type, is(CheckMojoException.Type.UNTAGGED));
            assertThat(e.isGraceful(), is(true));
        }
    }

    @Test
    public void testCheckTagSuccess() throws Exception {
        mojo.checkTag = true;

        when(repository.describe().isTagged()).thenReturn(true);

        mojo.run(repository);
    }

    @Test
    public void testCheckTagSuccessUnclean() throws Exception {
        Log log = mock(Log.class);
        mojo.setLog(log);
        mojo.checkTag = true;

        when(repository.describe().isTagged()).thenReturn(true);
        when(repository.isDirty(false)).thenReturn(true);

        mojo.run(repository);

        verify(log).warn("The current commit (`HEAD`) is tagged, but " +
                "the worktree is unclean. This is probably undesirable.");
    }

    @Test
    public void testGenericFailure() throws Exception {
        Throwable exception = new GitRepositoryException("");
        when(repository.describe()).thenThrow(exception);
        mojo.checkTag = true;

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (MavanagaiataMojoException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Error while checking repository.")));
            assertThat(e.isGraceful(), is(false));
        }
    }

    @Test
    public void testInitWithCommitMessagePattern() {
        mojo.checkCommitMessage = "Commit message";
        mojo.initConfiguration();

        assertThat(mojo.commitMessagePattern.pattern(), is(equalTo(mojo.checkCommitMessage)));
    }

}
