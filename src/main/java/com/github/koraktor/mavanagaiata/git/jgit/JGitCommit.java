/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.github.koraktor.mavanagaiata.git.GitCommit;

/**
 * Wrapper around JGit's {@link RevCommit} object to represent a Git commit
 *
 * @author Sebastian Staudt
 */
class JGitCommit implements GitCommit {

    protected PersonIdent author;
    protected RevCommit commit;
    private PersonIdent committer;

    /**
     * Creates a new instance from a JGit commit object
     *
     * @param commit The commit object to wrap
     */
    JGitCommit(RevCommit commit) {
        this.commit = commit;

        author    = commit.getAuthorIdent();
        committer = commit.getCommitterIdent();
    }

    /**
     * Compare an object to this one commit
     * <p>
     * An object is equal to this commit object if it is an instance of
     * {@code JGitCommit} and wraps around the same JGit commit object.
     *
     * @param object The object to check for equality
     * @return {@code true} if the object is equal to this one
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof JGitCommit &&
                commit.equals(((JGitCommit) object).commit);

    }

    public Date getAuthorDate() {
        return author.getWhen();
    }

    public String getAuthorEmailAddress() {
        return author.getEmailAddress();
    }

    public String getAuthorName() {
        return author.getName();
    }

    public TimeZone getAuthorTimeZone() {
        return author.getTimeZone();
    }

    public Date getCommitterDate() {
        return committer.getWhen();
    }

    public String getCommitterEmailAddress() {
        return committer.getEmailAddress();
    }

    public String getCommitterName() {
        return committer.getName();
    }

    public TimeZone getCommitterTimeZone() {
        return committer.getTimeZone();
    }

    public String getId() {
        return commit.getName();
    }

    public String getMessage() {
        return commit.getFullMessage();
    }

    public String getMessageSubject() {
        return commit.getShortMessage();
    }

    /**
     * Returns the hash code of the underlying commit's ID string
     *
     * @return The hash code for this commit
     */
    @Override
    public int hashCode() {
        return commit.getId().hashCode();
    }

    @Override
    public boolean isMergeCommit() {
        return commit.getParentCount() > 1;
    }
}
