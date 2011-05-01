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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * This goal provides the full ID of the current Git commit in the
 * "mavanagaiata.commit.id", "mavanagaiata.commit.sha", "mvngit.commit.id",
 * "mvngit.commit.sha" properties. The abbreviated commit ID is stored in the
 * "mavanagaiata.commit.abbrev" and "mvngit.commit.abbrev" properties.
 *
 * @author Sebastian Staudt
 * @goal commit
 * @phase initialize
 * @requiresProject
 */
public class GitCommitMojo extends AbstractMojo {

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
     * The ID (full and abbreviated) of the current Git commit out Git branch
     * is retrieved using a JGit Repository instance
     *
     * @see Repository
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void execute() throws MojoExecutionException {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository repository = repositoryBuilder
                .setGitDir(this.gitDir)
                .readEnvironment()
                .findGitDir()
                .build();

            RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(repository.getRef("HEAD")
                .getObjectId());
            String abbrevId = repository.getObjectDatabase().newReader()
                .abbreviate(commit, 8).name();
            String shaId = commit.getName();

            project.getProperties().put("mavanagaiata.commit.abbrev", abbrevId);
            project.getProperties().put("mavanagaiata.commit.id", shaId);
            project.getProperties().put("mavanagaiata.commit.sha", shaId);
            project.getProperties().put("mvngit.commit.abbrev", abbrevId);
            project.getProperties().put("mvngit.commit.id", shaId);
            project.getProperties().put("mvngit.commit.sha", shaId);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read Git repository", e);
        }
    }
}
