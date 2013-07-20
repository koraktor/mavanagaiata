/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.text.SimpleDateFormat;

import org.eclipse.jgit.revwalk.RevCommit;

import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

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
     * The ID (full and abbreviated) of the current Git commit out Git branch
     * is retrieved using a JGit Repository instance
     *
     * @see RevCommit#getName()
     * @see org.eclipse.jgit.lib.ObjectReader#abbreviate(org.eclipse.jgit.lib.AnyObjectId, int)
     * @throws MavanagaiataMojoException if retrieving information from the Git
     *         repository fails
     */
    public void run() throws MavanagaiataMojoException {
        try {
            GitCommit commit = this.repository.getHeadCommit();
            String abbrevId  = this.repository.getAbbreviatedCommitId();
            String shaId     = commit.getId();
            boolean isDirty  = false;

            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            dateFormat.setTimeZone(commit.getAuthorTimeZone());
            String authorDate = dateFormat.format(commit.getAuthorDate());
            dateFormat.setTimeZone(commit.getCommitterTimeZone());
            String commitDate = dateFormat.format(commit.getCommitterDate());

            if (this.repository.isDirty(this.dirtyIgnoreUntracked)) {
                abbrevId += this.dirtyFlag;
                shaId    += this.dirtyFlag;
                isDirty  = true;
            }

            this.addProperty("commit.abbrev", abbrevId);
            this.addProperty("commit.author.date", authorDate);
            this.addProperty("commit.author.name", commit.getAuthorName());
            this.addProperty("commit.author.email", commit.getAuthorEmailAddress());
            this.addProperty("commit.committer.date", commitDate);
            this.addProperty("commit.committer.name", commit.getCommitterName());
            this.addProperty("commit.committer.email", commit.getCommitterEmailAddress());
            this.addProperty("commit.id", shaId);
            this.addProperty("commit.sha", shaId);
            this.addProperty("commit.dirty", String.valueOf(isDirty));
        } catch (GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to read Git commit information", e);
        }
    }
}
