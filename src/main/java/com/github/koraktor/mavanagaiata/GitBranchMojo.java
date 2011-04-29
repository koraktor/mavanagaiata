package com.github.koraktor.mavanagaiata;

/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * This goal provides the currently checked out Git branch in the
 * "mavanagaiata.branch" and "mvngit.branch" properties.
 *
 * @author Sebastian Staudt
 * @goal branch
 * @phase initialize
 * @requiresProject
 */
public class GitBranchMojo extends AbstractMojo {

    /**
     * The project base directory
     *
     * @parameter expression="${basedir}/.git"
     * @required
     */
    private File gitDir;

    /**
     * The maven project
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     *
     */
    public void execute() throws MojoExecutionException {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository repository = repositoryBuilder
                .setGitDir(this.gitDir)
                .readEnvironment()
                .findGitDir()
                .build();

            project.getProperties().put("mavanagaiata.branch", repository.getBranch());
            project.getProperties().put("mvngit.branch", repository.getBranch());
        } catch (IOException e) {
            this.getLog().error("Unable to read Git repository");
        }
    }
}
