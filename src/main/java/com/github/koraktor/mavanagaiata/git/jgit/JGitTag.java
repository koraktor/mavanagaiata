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
import org.eclipse.jgit.revwalk.RevTag;

import com.github.koraktor.mavanagaiata.git.GitTag;

/**
 * Wrapper around JGit's {@link RevTag} object to represent a Git tag
 *
 * @author Sebastian Staudt
 */
class JGitTag implements GitTag {

    protected RevTag tag;

    PersonIdent taggerIdent;

    /**
     * Creates a new instance from a JGit tag object
     *
     * @param tag The tag object to wrap
     */
    JGitTag(RevTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof JGitTag &&
                tag.equals(((JGitTag) object).tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    public Date getDate() {
        return taggerIdent.getWhen();
    }

    public String getName() {
        return tag.getTagName();
    }

    public TimeZone getTimeZone() {
        return taggerIdent.getTimeZone();
    }

    @Override
    public boolean isLoaded() {
        return taggerIdent != null;
    }

}
