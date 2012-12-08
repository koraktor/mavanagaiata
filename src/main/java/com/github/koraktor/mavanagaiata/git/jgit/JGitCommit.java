/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
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
public class JGitCommit implements GitCommit {

    protected PersonIdent author;

    protected RevCommit commit;

    protected PersonIdent committer;

    /**
     * Creates a new instance from a JGit commit object
     *
     * @param commit The commit object to wrap
     */
    public JGitCommit(RevCommit commit) {
        this.author    = commit.getAuthorIdent();
        this.commit    = commit;
        this.committer = commit.getCommitterIdent();
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
        if (object instanceof JGitCommit) {
            return this.commit.equals(((JGitCommit) object).commit);
        }

        return false;
    }

    public Date getAuthorDate() {
        return this.author.getWhen();
    }

    public String getAuthorEmailAddress() {
        return this.author.getEmailAddress();
    }

    public String getAuthorName() {
        return this.author.getName();
    }

    public TimeZone getAuthorTimeZone() {
        return this.author.getTimeZone();
    }

    public Date getCommitterDate() {
        return this.committer.getWhen();
    }

    public String getCommitterEmailAddress() {
        return this.committer.getEmailAddress();
    }

    public String getCommitterName() {
        return this.committer.getName();
    }

    public TimeZone getCommitterTimeZone() {
        return this.committer.getTimeZone();
    }

    public String getId() {
        return this.commit.getName();
    }

    public String getMessageSubject() {
        return this.commit.getShortMessage();
    }

    /**
     * Returns the hash code of the unterlying commit's ID string
     *
     * @return The hash code for this commit
     */
    @Override
    public int hashCode() {
        return this.commit.getId().hashCode();
    }

}
