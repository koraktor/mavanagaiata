/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2016-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.util.regex.Pattern;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

/**
 * This goal checks various aspects of a Git repository to ensure it is in a
 * valid state prior to a build
 * <p>
 * The following checks are available:
 * <ul>
 * <li>Clean working directory (enabled by default)
 * <li>Tagged commit
 * <li>Branch name
 * <li>Commit message
 * </ul>
 *
 * @author Sebastian Staudt
 * @since 0.8.0
 */
@Mojo(name = "check",
      defaultPhase = LifecyclePhase.VALIDATE,
      threadSafe = true)
public class CheckMojo extends AbstractGitMojo {

    Pattern commitMessagePattern;

    /**
     * Check whether the current branch is the given branch
     */
    @Parameter(property = "mavanagaiata.checkBranch")
    String checkBranch;

    /**
     * Check if the working directory is clean
     */
    @Parameter(property = "mavanagaiata.checkClean",
                   defaultValue = "true")
    boolean checkClean;

    /**
     * Check whether the message of the current commit matches the given format
     */
    @Parameter(property = "mavanagaiata.checkCommitMessage")
    String checkCommitMessage;

    /**
     * Check whether the current commit is tagged
     */
    @Parameter(property = "mavanagaiata.requireTag",
               defaultValue = "false")
    boolean checkTag;

    @Override
    protected GitRepository init() throws MavanagaiataMojoException {
        initConfiguration();

        return super.init();
    }

    /**
     * Compiles the commit message check regex
     */
    void initConfiguration() {
        if (checkCommitMessage != null) {
            commitMessagePattern = Pattern.compile(checkCommitMessage, Pattern.MULTILINE);
        }
    }

    @Override
    protected void run(GitRepository repository)
            throws MavanagaiataMojoException {
        if (!head.equals(GitRepository.DEFAULT_HEAD)) {
            getLog().warn("Your configuration specifies `" + head +
                "` (instead of `" + GitRepository.DEFAULT_HEAD + "`) " +
                "as the current commit. The results of these checks " +
                "might not match the actual repository state.");
        }

        try {
            checkBranch(repository);
            checkCommitMessage(repository);
            checkClean(repository);
            checkTag(repository);
        } catch (GitRepositoryException e) {
            throw new MavanagaiataMojoException("Error while checking repository.", e);
        }
    }

    /**
     * Checks if the branch matches the configured name
     *
     * @param repository The repository instance to check
     * @see #checkBranch
     * @throws CheckMojoException if the branch does not match
     * @throws GitRepositoryException if the current branch cannot be retrieved
     */
    private void checkBranch(GitRepository repository)
            throws CheckMojoException, GitRepositoryException {
        if (checkBranch != null && !repository.getBranch().equals(checkBranch)) {
            throw new CheckMojoException(CheckMojoException.Type.WRONG_BRANCH, repository.getBranch(), checkBranch);
        }
    }

    /**
     * Checks if the worktree is in a clean state
     *
     * @param repository The repository instance to check
     * @see #checkClean
     * @throws CheckMojoException if the worktree is not clean
     * @throws GitRepositoryException if the worktree state cannot be retrieved
     */
    private void checkClean(GitRepository repository)
            throws CheckMojoException, GitRepositoryException{
        if (checkClean && repository.isDirty(dirtyIgnoreUntracked)) {
            throw new CheckMojoException(CheckMojoException.Type.UNCLEAN);
        }
    }

    /**
     * Checks if commit message matches the configured pattern
     *
     * @param repository The repository instance to check
     * @see #checkCommitMessage
     * @throws CheckMojoException if the commit message does not match
     * @throws GitRepositoryException if the current commit cannot be retrieved
     */
    private void checkCommitMessage(GitRepository repository)
            throws GitRepositoryException, CheckMojoException {
        if (commitMessagePattern != null && !commitMessagePattern.matcher(repository.getHeadCommit().getMessage()).find()) {
            throw new CheckMojoException(CheckMojoException.Type.WRONG_COMMIT_MSG, checkCommitMessage);
        }
    }

    /**
     * Checks if the the current {@code HEAD} is tagged
     *
     * @param repository The repository instance to check
     * @see #checkTag
     * @throws CheckMojoException if {@code HEAD} is not tagged
     * @throws GitRepositoryException if the commit cannot be described
     */
    private void checkTag(GitRepository repository)
            throws GitRepositoryException, CheckMojoException {
        if (checkTag) {
            if (!repository.describe().isTagged()) {
                throw new CheckMojoException(CheckMojoException.Type.UNTAGGED);
            }

            if (!checkClean && repository.isDirty(dirtyIgnoreUntracked)) {
                getLog().warn("The current commit (`" + head +
                    "`) is tagged, but the worktree is unclean. This " +
                    "is probably undesirable.");
            }
        }
    }

}
