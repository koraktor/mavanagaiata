/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * Represents information about a Git commit like supplied by
 * {@code git describe}
 *
 * @author Sebastian Staudt
 */
public class GitTagDescription {

    private String abbreviatedCommitId;

    private int distance;

    private GitTag nextTag;

    /**
     * Create a new description for the given information
     *
     * @param abbreviatedCommitId The abbreviated commit ID
     * @param nextTag The next tag reachable from the commit
     * @param distance The distance to the next tag
     */
    public GitTagDescription(String abbreviatedCommitId, GitTag nextTag, int distance) {
        this.abbreviatedCommitId = abbreviatedCommitId;
        this.distance            = distance;
        this.nextTag             = nextTag;
    }

    /**
     * Returns the name of the next reachable tag
     *
     * @return The name of the next tag
     */
    public String getNextTagName() {
        return (this.nextTag == null) ? "" : this.nextTag.getName();
    }

    /**
     * Returns whether the commit is tagged
     *
     * @return {@code true} if the commit is tagged
     */
    public boolean isTagged() {
        return distance == 0;
    }

    /**
     * Returns the string representation of this description
     * <p>
     * This includes the abbreviated commit ID and (if available) the distance
     * to and the name of the next tag.
     *
     * @return The string representation of this description
     */
    @Override
    public String toString() {
        if (this.nextTag == null) {
            return this.abbreviatedCommitId;
        } else if (this.distance == 0) {
            return this.nextTag.getName();
        } else {
            return String.format("%s-%d-g%s",
                this.nextTag.getName(),
                this.distance,
                this.abbreviatedCommitId);
        }
    }

}
