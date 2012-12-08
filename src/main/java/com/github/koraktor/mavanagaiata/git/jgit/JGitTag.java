/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevTag;

import com.github.koraktor.mavanagaiata.git.GitTag;

/**
 * Wrapper around JGit's {@link RevTag} object to represent a Git tag
 *
 * @author Sebastian Staudt
 */
public class JGitTag implements GitTag {

    protected RevTag tag;

    /**
     * Creates a new instance from a JGit tag object
     *
     * @param tag The tag object to wrap
     */
    public JGitTag(RevTag tag) {
        this.tag = tag;
    }

    public Date getDate() {
        return this.tag.getTaggerIdent().getWhen();
    }

    public String getName() {
        return this.tag.getTagName();
    }

    public TimeZone getTimeZone() {
        return this.tag.getTaggerIdent().getTimeZone();
    }

}
