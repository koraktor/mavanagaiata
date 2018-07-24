/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;

/**
 * This class represents a tag candidate which could be the latest tag in the
 *
 */
class JGitTagCandidate {
    private final JGitTag tag;
    private final RevFlag flag;
    private int distance;

    /**
     * Create a new tag candidate instance with the given attributes
     *
     * @param tag The candidate tag
     * @param distance The distance from the current commit
     * @param flag The flags used to determine ancestor commits
     */
    JGitTagCandidate(JGitTag tag, int distance, RevFlag flag) {
        this.tag = tag;
        this.distance = distance;
        this.flag = flag;
    }

    /**
     * @return The distance from the current commit
     */
    int getDistance() {
        return distance;
    }

    /**
     * @return The candidate tag
     */
    public JGitTag getTag() {
        return tag;
    }

    /**
     * Increments the distance of this tag candidate if the given commit
     * has not been seen already
     *
     * @param commit The commit to check
     */
    void incrementDistanceIfExcludes(RevCommit commit) {
        if (!commit.has(flag)) {
            distance ++;
        }
    }
}
