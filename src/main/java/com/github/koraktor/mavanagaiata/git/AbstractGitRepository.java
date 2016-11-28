/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2014, Sebastian Staudt
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

    protected MailMap mailMap;

    /**
     * Call the {@code #close()} method of this repository once it isn't used
     * anymore
     *
     * @see #close
     */
    @Override
    public void finalize() throws Throwable {
        this.close();

        super.finalize();
    }

    public String getAbbreviatedCommitId() throws GitRepositoryException {
        return this.getAbbreviatedCommitId(this.getHeadCommit());
    }

    public MailMap getMailMap() throws GitRepositoryException {
        if (this.mailMap == null) {
            this.mailMap = new MailMap();
            this.mailMap.parseMailMap(this);
        }

        return this.mailMap;
    }

    public void setHeadRef(String headRef) {
        this.headRef = headRef;
    }

}
