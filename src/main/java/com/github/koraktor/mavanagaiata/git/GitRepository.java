/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.File;
import java.util.Map;

/**
 * This interface specifies the basic properties needed for the mojos to access
 * the information about a Git repository
 *
 * @author Sebastian Staudt
 */
public interface GitRepository extends AutoCloseable {

    /**
     * The default head ref
     */
    String DEFAULT_HEAD = "HEAD";

    /**
     * Checks whether the Git repository is accessible.
     *
     * @throws GitRepositoryException if the repository is not accessible.
     */
    void check() throws GitRepositoryException;

    /**
     * Closes any resources that are needed to access this repository
     */
    void close();

    /**
     * Describes the current Git commit like {@code git describe} does
     *
     * @return The description of the current {@code HEAD} commit
     * @throws GitRepositoryException if the description cannot be created
     */
    GitTagDescription describe() throws GitRepositoryException;

    /**
     * Returns the abbreviated commit SHA ID of the current Git commit
     *
     * @return The abbreviated commit ID of the current {@code HEAD} commit
     * @throws GitRepositoryException if the abbreviated commit ID cannot be
     *         determined
     */
    String getAbbreviatedCommitId() throws GitRepositoryException;

    /**
     * Returns the abbreviated commit SHA ID of the given Git commit
     *
     * @param commit The Git commit to get the abbreviated ID for
     * @return The abbreviated commit ID of the given commit
     * @throws GitRepositoryException if the abbreviated commit ID cannot be
     *         determined
     */
    String getAbbreviatedCommitId(GitCommit commit)
        throws GitRepositoryException;

    /**
     * Returns the currently checked out branch of the Git repository
     *
     * @return The current branch of the Git repository
     * @throws GitRepositoryException if the current branch cannot be
     *         determined
     */
    String getBranch() throws GitRepositoryException;

    /**
     * Returns the current {@code HEAD} commit of the Git repository
     *
     * @return The current commit of the Git Repository
     * @throws GitRepositoryException if the current commit cannot be
     *         determined
     */
    GitCommit getHeadCommit() throws GitRepositoryException;

    /**
     * Returns the Git ref used as the {@code HEAD} commit of the repository
     *
     * @return The ref used as {@code HEAD}
     */
    String getHeadRef();

    /**
     * Returns a {@code MailMap} object that holds information from Git's
     * {@code .mailmap} file
     *
     * @return A {@code .mailmap} representation or {@code null} if none exits
     * @throws GitRepositoryException if the {@code .mailmap} file cannot be
     *         read or parsed
     */
    MailMap getMailMap() throws GitRepositoryException;

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
    Map<String, GitTag> getTags() throws GitRepositoryException;

    /**
     * Returns the worktree of the repository
     *
     * @return The worktree of the repository
     */
    File getWorkTree();

    /**
     * Returns whether this repository instance has been checked
     *
     * @return {@code true} if this repository has already been checked
     * @see #check
     */
    boolean isChecked();

    /**
     * Returns whether the worktree of the repository is in a clean state
     *
     * @param ignoreUntracked If {@code true}, untracked files in the
     *        repository will be ignored
     * @return {@code true} if there are modified files in the repository's
     *         worktree
     * @throws GitRepositoryException if an error occurs while checking the
     *         worktree state
     */
    boolean isDirty(boolean ignoreUntracked) throws GitRepositoryException;

    /**
     * Returns whether this repository is currently on an “unborn” branch
     *
     * An “unborn” branch is a branch without any actual commits. This only
     * applies when the configured head ref is actually {@code HEAD}.
     * Otherwise a configuration error is assumed.
     *
     * @return {@code true} if the current branch is
     * @throws GitRepositoryException if an error occurs while retrieving the
     *         current HEAD commit
     */
    boolean isOnUnbornBranch() throws GitRepositoryException;

    /**
     * Sets the Git ref to use as the {@code HEAD} commit of the repository
     *
     * @param headRef The ref to use as {@code HEAD}
     */
    void setHeadRef(String headRef);

    /**
     * Runs the given action for all commits reachable from the current
     * {@code HEAD} commit
     *
     * @param action The action to execute for each commit found
     * @param <T> The action’s type
     * @return The action itself
     * @throws GitRepositoryException if an error occurs during walking through
     *         the commits
     */
    <T extends CommitWalkAction> T walkCommits(T action)
            throws GitRepositoryException;

}
