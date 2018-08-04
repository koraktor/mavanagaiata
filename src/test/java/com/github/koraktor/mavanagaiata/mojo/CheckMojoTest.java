/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2016-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.apache.maven.plugin.logging.Log;

import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
class CheckMojoTest extends MojoAbstractTest<CheckMojo> {

    @Test
    void testChangedHead() throws MavanagaiataMojoException {
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
    void testCheckBranchFailed() throws Exception {
        mojo.checkBranch = "production";

        when(repository.getBranch()).thenReturn("master");

        CheckMojoException e = assertThrows(CheckMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.type, is(CheckMojoException.Type.WRONG_BRANCH));
        assertThat(e.isGraceful(), is(true));
    }

    @Test
    void testCheckBranchSuccess() throws Exception {
        mojo.checkBranch = "production";

        when(repository.getBranch()).thenReturn("production");

        mojo.run(repository);
    }

    @Test
    void testCheckCleanFailed() throws Exception {
        mojo.checkClean = true;

        when(repository.isDirty(false)).thenReturn(true);

        CheckMojoException e = assertThrows(CheckMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.type, is(CheckMojoException.Type.UNCLEAN));
        assertThat(e.isGraceful(), is(true));
    }

    @Test
    void testCheckCleanSuccess() throws Exception {
        mojo.checkClean = true;

        when(repository.isDirty(false)).thenReturn(false);

        mojo.run(repository);
    }

    @Test
    void testCheckCommitMessageFailed() throws Exception {
        mojo.checkCommitMessage = "Release";
        mojo.initConfiguration();

        when(repository.getHeadCommit().getMessage()).thenReturn("Fix some bugs");

        CheckMojoException e = assertThrows(CheckMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.type, is(CheckMojoException.Type.WRONG_COMMIT_MSG));
        assertThat(e.isGraceful(), is(true));
    }

    @Test
    void testCheckCommitMessageSuccess() throws Exception {
        mojo.checkCommitMessage = "Release";
        mojo.initConfiguration();

        when(repository.getHeadCommit().getMessage()).thenReturn("Release 1.0.0");

        mojo.run(repository);
    }

    @Test
    void testCheckTagFailed() throws Exception {
        mojo.checkTag = true;

        when(repository.describe().isTagged()).thenReturn(false);

        CheckMojoException e = assertThrows(CheckMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.type, is(CheckMojoException.Type.UNTAGGED));
        assertThat(e.isGraceful(), is(true));
    }

    @Test
    void testCheckTagSuccess() throws Exception {
        mojo.checkTag = true;

        when(repository.describe().isTagged()).thenReturn(true);

        mojo.run(repository);
    }

    @Test
    void testCheckTagSuccessUnclean() throws Exception {
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
    void testGenericFailure() throws Exception {
        Throwable exception = new GitRepositoryException("");
        when(repository.describe()).thenThrow(exception);
        mojo.checkTag = true;

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getCause(), is(exception));
        assertThat(e.getMessage(), is(equalTo("Error while checking repository.")));
        assertThat(e.isGraceful(), is(false));
    }

    @Test
    void testInitWithCommitMessagePattern() {
        mojo.checkCommitMessage = "Commit message";
        mojo.initConfiguration();

        assertThat(mojo.commitMessagePattern.pattern(), is(equalTo(mojo.checkCommitMessage)));
    }

}
