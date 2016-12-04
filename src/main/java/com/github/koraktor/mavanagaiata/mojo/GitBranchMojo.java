/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

/**
 * This goal provides the currently checked out Git branch in the
 * "mavanagaiata.branch" and "mvngit.branch" properties.
 *
 * @author Sebastian Staudt
 * @since 0.1.0
 */
@Mojo(name ="branch",
      defaultPhase = LifecyclePhase.INITIALIZE,
      threadSafe = true)
public class GitBranchMojo extends AbstractGitMojo {

    /**
     * Information about the currently checked out Git branch is retrieved
     * using a JGit Repository instance
     *
     * @see org.eclipse.jgit.lib.Repository#getBranch()
     * @throws MavanagaiataMojoException if retrieving information from the Git
     *         repository fails
     */
    public void run(GitRepository repository) throws MavanagaiataMojoException {
        try {
            addProperty("branch", repository.getBranch());
        } catch(GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to read Git branch", e);
        }
    }

}
