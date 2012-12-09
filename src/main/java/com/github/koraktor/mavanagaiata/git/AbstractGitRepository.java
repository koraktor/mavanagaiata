/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * An abstract implementation of a Git repository that provides basic and
 * common functionality
 *
 * @author Sebastian Staudt
 */
public abstract class AbstractGitRepository implements GitRepository {

    protected String headRef;

    /**
     * Call the {@code #close()} method of this repository once it isn't used
     * anymore
     *
     * @see #close
     */
    @Override
    public void finalize() {
        this.close();
    }

    public String getAbbreviatedCommitId() throws GitRepositoryException {
        return this.getAbbreviatedCommitId(this.getHeadCommit());
    }

    public void setHeadRef(String headRef) {
        this.headRef = headRef;
    }

}
