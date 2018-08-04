/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

/**
 *
 *
 * @author Sebastian Staudt
 */
class CommitWalkActionTest {

    class GenericCommitWalkAction extends CommitWalkAction {

        protected void run() {}

    }

    @Test
    void testExecute() throws Exception {
        CommitWalkAction action = spy(new GenericCommitWalkAction());
        GitCommit commit = mock(GitCommit.class);

        action.execute(commit);

        assertThat(action.currentCommit, is(commit));
        verify(action).run();
    }

}
