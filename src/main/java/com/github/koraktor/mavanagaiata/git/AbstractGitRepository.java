/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
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
    MailMap mailMap;

    public String getAbbreviatedCommitId() throws GitRepositoryException {
        return getAbbreviatedCommitId(getHeadCommit());
    }

    public MailMap getMailMap() throws GitRepositoryException {
        if (mailMap == null) {
            mailMap = new MailMap(this);
            mailMap.parseMailMap();
        }

        return mailMap;
    }

    public void setHeadRef(String headRef) {
        this.headRef = headRef;
    }

}
