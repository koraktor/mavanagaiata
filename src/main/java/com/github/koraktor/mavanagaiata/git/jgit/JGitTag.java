/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2017, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevTag;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;

/**
 * Wrapper around JGit's {@link RevTag} object to represent a Git tag
 *
 * @author Sebastian Staudt
 */
public class JGitTag implements GitTag {

    protected RevTag tag;

    protected PersonIdent taggerIdent;

    /**
     * Creates a new instance from a JGit tag object
     *
     * @param tag The tag object to wrap
     */
    public JGitTag(RevTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof JGitTag &&
                tag.equals(((JGitTag) object).tag);
    }

    public Date getDate() {
        return taggerIdent.getWhen();
    }

    public String getName() {
        return this.tag.getTagName();
    }

    public TimeZone getTimeZone() {
        return taggerIdent.getTimeZone();
    }

    @Override
    public void load(GitRepository repository) throws GitRepositoryException {
        if (taggerIdent != null) {
            return;
        }

        try {
            ((JGitRepository) repository).getRevWalk().parseBody(tag);
        } catch (IOException e) {
            throw new GitRepositoryException("Failed to load tag meta data.", e);
        }

        taggerIdent = tag.getTaggerIdent();
        tag.disposeBody();
    }

}
