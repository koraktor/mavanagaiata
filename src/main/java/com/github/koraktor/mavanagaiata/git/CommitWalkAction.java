/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
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

    protected GitRepository repository;

    /**
     * Executes this action for the given commit
     *
     * @param commit The current commit
     * @throws GitRepositoryException if an error occurs during the action
     */
    public void execute(GitCommit commit) throws GitRepositoryException {
        this.currentCommit = commit;
        this.run();
    }

    /**
     * Prepare the walk action
     * <p>
     * This can be used to load required data for the walk action, e.g. all
     * tags.
     */
    public void prepare() throws GitRepositoryException {}

    /**
     * The code of the action that should be executed for each commit during a
     * commit walk
     *
     * @throws GitRepositoryException if an error occurs during the action
     */
    protected abstract void run() throws GitRepositoryException;

    /**
     * Sets the repository this action should be executed in
     *
     * @param repository The repository for this action
     */
    public void setRepository(GitRepository repository) {
        this.repository = repository;
    }

}
