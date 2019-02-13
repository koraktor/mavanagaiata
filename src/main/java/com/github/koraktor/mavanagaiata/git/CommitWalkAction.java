/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * Basic implementation of an action that is executed for each commit during a
 * commit walk
 *
 * @author Sebastian Staudt
 */
public abstract class CommitWalkAction {

    protected GitCommit currentCommit;

    /**
     * Executes this action for the given commit
     *
     * @param commit The current commit
     * @throws GitRepositoryException if an error occurs during the action
     */
    public void execute(GitCommit commit) throws GitRepositoryException {
        currentCommit = commit;
        run();
    }

    /**
     * The code of the action that should be executed for each commit during a
     * commit walk
     *
     * @throws GitRepositoryException if an error occurs during the action
     */
    protected abstract void run() throws GitRepositoryException;

}
