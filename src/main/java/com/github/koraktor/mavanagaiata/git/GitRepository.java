/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.util.Map;

/**
 * This interface specifies the basic properties needed for the mojos to access
 * the information about a Git repository
 *
 * @author Sebastian Staudt
 */
public interface GitRepository {

    /**
     * Checks whether the Git repository is accessible
     *
     * @throws GitRepositoryException if the repository is not accessible
     */
    public void check() throws GitRepositoryException;

    /**
     * Closes any resources that are needed to access this repository
     */
    public void close();

    /**
     * Describes the current Git commit like {@code git describe} does
     *
     * @return The description of the current {@code HEAD} commit
     * @throws GitRepositoryException if the description cannot be created
     */
    public GitTagDescription describe() throws GitRepositoryException;

    /**
     * Returns the abbreviated commit SHA ID of the current Git commit
     *
     * @return The abbreviated commit ID of the current {@code HEAD} commit
     * @throws GitRepositoryException if the abbreviated commit ID cannot be
     *         determined
     */
    public String getAbbreviatedCommitId() throws GitRepositoryException;

    /**
     * Returns the abbreviated commit SHA ID of the given Git commit
     *
     * @param commit The Git commit to get the abbreviated ID for
     * @return The abbreviated commit ID of the given commit
     * @throws GitRepositoryException if the abbreviated commit ID cannot be
     *         determined
     */
    public String getAbbreviatedCommitId(GitCommit commit)
        throws GitRepositoryException;

    /**
     * Returns the currently checked out branch of the Git repository
     *
     * @return The current branch of the Git repository
     * @throws GitRepositoryException if the current branch cannot be
     *         determined
     */
    public String getBranch() throws GitRepositoryException;

    /**
     * Returns the current {@code HEAD} commit of the Git repository
     *
     * @return The current commit of the Git Repository
     * @throws GitRepositoryException if the current commit cannot be
     *         determined
     */
    public GitCommit getHeadCommit() throws GitRepositoryException;

    /**
     * Returns a map of tags available in this repository
     * <p>
     * The keys of the map are the SHA IDs of the objects referenced by the
     * tags. The map's values are the tags themselves.
     * <p>
     * <em>Note</em>: Only annotated tags referencing commit objects will be
     * returned.
     *
     * @return A map of tags in this repository
     * @throws GitRepositoryException if an error occurs while determining the
     *         tags in this repository
     */
    public Map<String, GitTag> getTags() throws GitRepositoryException;

    /**
     * Returns whether the worktree of the repository is in a clean state
     *
     * @return {@code true} if there are modified files in the repository's
     *         worktree
     * @throws GitRepositoryException if an error occurs while checking the
     *         worktree state
     */
    public boolean isDirty() throws GitRepositoryException;

    /**
     * Sets the Git ref to use as the {@code HEAD} commit of the repository
     *
     * @param headRef The ref to use as {@code HEAD}
     */
    public void setHeadRef(String headRef);

    /**
     * Runs the given action for all commits reachable from the current
     * {@code HEAD} commit
     *
     * @param action The action to execute for each commit found
     * @throws GitRepositoryException if an error occurs during walking through
     *         the commits
     */
    public void walkCommits(CommitWalkAction action)
        throws GitRepositoryException;

}
