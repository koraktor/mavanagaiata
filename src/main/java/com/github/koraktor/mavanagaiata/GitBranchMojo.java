/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * This goal provides the currently checked out Git branch in the
 * "mavanagaiata.branch" and "mvngit.branch" properties.
 *
 * @author Sebastian Staudt
 * @goal branch
 * @phase initialize
 * @requiresProject
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
    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        try {
            project.getProperties().put("mavanagaiata.branch", this.repository.getBranch());
            project.getProperties().put("mvngit.branch", this.repository.getBranch());
        } catch(IOException e) {
            throw new MojoExecutionException("Unable to read Git branch", e);
        }
    }
}
