/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.Date;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.revwalk.RevCommit;

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
public class GitCommitMojo extends AbstractGitMojo {

    /**
     * The ID (full and abbreviated) of the current Git commit out Git branch
     * is retrieved using a JGit Repository instance
     *
     * @see RevCommit#getName()
     * @see org.eclipse.jgit.lib.ObjectReader#abbreviate(org.eclipse.jgit.lib.AnyObjectId, int)
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void execute() throws MojoExecutionException {
        try {
            RevCommit commit = this.getHead();
            String abbrevId = this.repository.getObjectDatabase().newReader()
                .abbreviate(commit, 8).name();
            String shaId = commit.getName();
            Date date = new Date(new Long(commit.getCommitTime()) * 1000);

            project.getProperties().put("mavanagaiata.commit.abbrev", abbrevId);
            project.getProperties().put("mavanagaiata.commit.date", date);
            project.getProperties().put("mavanagaiata.commit.id", shaId);
            project.getProperties().put("mavanagaiata.commit.sha", shaId);
            project.getProperties().put("mvngit.commit.abbrev", abbrevId);
            project.getProperties().put("mvngit.commit.date", date);
            project.getProperties().put("mvngit.commit.id", shaId);
            project.getProperties().put("mvngit.commit.sha", shaId);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read Git commit information", e);
        }
    }
}
