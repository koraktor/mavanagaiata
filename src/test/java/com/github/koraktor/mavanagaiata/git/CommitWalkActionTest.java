/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import org.junit.Test;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 *
 *
 * @author Sebastian Staudt
 */
public class CommitWalkActionTest {

    class GenericCommitWalkAction extends CommitWalkAction {

        protected void run() throws GitRepositoryException {}

    }

    @Test
    public void testExecute() throws Exception {
        CommitWalkAction action = spy(new GenericCommitWalkAction());
        GitCommit commit = mock(GitCommit.class);

        action.execute(commit);

        assertThat(action.currentCommit, is(commit));
        verify(action).run();
    }

}
