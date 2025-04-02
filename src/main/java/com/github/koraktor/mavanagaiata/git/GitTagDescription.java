/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2025, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * Represents information about a Git commit like supplied by
 * {@code git describe}
 *
 * @author Sebastian Staudt
 */
public class GitTagDescription {

    private static final String DESCRIBE_FORMAT = "%s-%d-g%s";

    private final String abbrevCommitId;

    private final int distance;

    private final String nextTagName;

    /**
     * Create a new description for the given information
     *
     * @param abbrevCommitId The abbreviated commit ID
     * @param nextTagName The name of the next tag reachable from the commit
     * @param distance The distance to the next tag
     */
    public GitTagDescription(String abbrevCommitId, String nextTagName, int distance) {
        this.abbrevCommitId = abbrevCommitId;
        this.distance = distance;
        this.nextTagName = nextTagName;
    }

    /**
     * Returns the name of the next reachable tag
     *
     * @return The name of the next tag
     */
    public String getNextTagName() {
        return (nextTagName == null) ? "" : nextTagName;
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
        if (nextTagName == null) {
            return abbrevCommitId;
        }

        if (distance == 0) {
            return nextTagName;
        }

        return String.format(DESCRIBE_FORMAT,
            nextTagName,
            distance,
            abbrevCommitId);
    }

}
