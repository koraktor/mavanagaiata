/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.apache.maven.plugin.MojoExecutionException;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

/**
 * This goal provides the currently checked out Git branch in the
 * "mavanagaiata.branch" and "mvngit.branch" properties.
 *
 * @author Sebastian Staudt
 * @goal branch
 * @phase initialize
 * @requiresProject
 * @since 0.1.0
 */
public class GitBranchMojo extends AbstractGitMojo {

    /**
     * Information about the currently checked out Git branch is retrieved
     * using a JGit Repository instance
     *
     * @see org.eclipse.jgit.lib.Repository#getBranch()
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void run() throws MojoExecutionException {
        try {
            this.addProperty("branch", this.repository.getBranch());
        } catch(GitRepositoryException e) {
            throw new MojoExecutionException("Unable to read Git branch", e);
        }
    }

}
