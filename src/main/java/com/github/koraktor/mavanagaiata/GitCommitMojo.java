/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * This goal provides the full ID of the current Git commit in the
 * "mavanagaiata.commit.id", "mavanagaiata.commit.sha", "mvngit.commit.id",
 * "mvngit.commit.sha" properties. The abbreviated commit ID is stored in the
 * "mavanagaiata.commit.abbrev" and "mvngit.commit.abbrev" properties.
 * Additionally the author's and committer's name and email address are stored
 * in the properties "mavanagaiata.commit.author.name",
 * "mavanagaiata.commit.author.email", "mvngit.commit.auhtor.name" and
 * "mvngit.commit.author.email", and "mavanagaiata.commit.committer.name",
 * "mavanagaiata.commit.committer.email", "mvngit.commit.commiter.name" and
 * "mvngit.commit.committer.email" respectively.
 *
 * @author Sebastian Staudt
 * @goal commit
 * @phase initialize
 * @requiresProject
 * @since 0.1.0
 */
public class GitCommitMojo extends AbstractGitMojo {

    /**
     * The date format to use for committer and author dates
     *
     * @parameter expression="${mavanagaiata.commit.dateFormat}"
     * @since 0.2.1
     */
    protected String dateFormat = baseDateFormat;

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
                .abbreviate(commit).name();
            PersonIdent author = commit.getAuthorIdent();
            PersonIdent committer = commit.getCommitterIdent();
            String shaId = commit.getName();
            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            dateFormat.setTimeZone(author.getTimeZone());
            String authorDate = dateFormat.format(author.getWhen());
            dateFormat.setTimeZone(author.getTimeZone());
            String commitDate = dateFormat.format(committer.getWhen());

            if (this.isDirty()) {
                abbrevId += this.dirtyFlag;
                shaId    += this.dirtyFlag;
            }

            this.addProperty("commit.abbrev", abbrevId);
            this.addProperty("commit.author.date", authorDate);
            this.addProperty("commit.author.name", author.getName());
            this.addProperty("commit.author.email", author.getEmailAddress());
            this.addProperty("commit.committer.date", commitDate);
            this.addProperty("commit.committer.name", committer.getName());
            this.addProperty("commit.committer.email", committer.getEmailAddress());
            this.addProperty("commit.id", shaId);
            this.addProperty("commit.sha", shaId);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read Git commit information", e);
        } finally {
            this.cleanup();
        }
    }
}
