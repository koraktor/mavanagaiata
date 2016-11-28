/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * Represents information about a Git commit like supplied by
 * {@code git describe}
 *
 * @author Sebastian Staudt
 */
public class GitTagDescription {

    protected String abbreviatedCommitId;

    protected GitCommit commit;

    protected int distance;

    protected GitTag nextTag;

    /**
     * Create a new description for the given information
     *
     * @param repository The repository which contains the commit
     * @param commit The commit
     * @param nextTag The next tag reachable from the commit
     * @param distance The distance to the next tag
     * @throws GitRepositoryException if the commit can not be resolved
     */
    public GitTagDescription(GitRepository repository, GitCommit commit,
                             GitTag nextTag, int distance)
            throws GitRepositoryException {
        this.abbreviatedCommitId = repository.getAbbreviatedCommitId(commit);
        this.commit              = commit;
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
