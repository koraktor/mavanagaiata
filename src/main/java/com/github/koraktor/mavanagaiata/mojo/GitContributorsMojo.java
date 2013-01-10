/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.apache.maven.plugin.MojoExecutionException;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

/**
 * This goal allows to generate a list of contributors for the currently
 * checked out branch of the Git repository. It will list all authors of the
 * commits in this branch. It can be configured to display the changelog or
 * save it to a file.
 *
 * @author Sebastian Staudt
 * @goal contributors
 * @phase process-resources
 * @requiresProject
 * @since 0.2.0
 */
public class GitContributorsMojo extends AbstractGitOutputMojo {

    protected final static Comparator<Contributor> COUNT_COMPARATOR = new Comparator<Contributor>() {
        public int compare(Contributor contributor1, Contributor contributor2) {
            return -contributor1.count.compareTo(contributor2.count);
        }
    };

    protected final static Comparator<Contributor> DATE_COMPARATOR = new Comparator<Contributor>() {
        public int compare(Contributor contributor1, Contributor contributor2) {
            return contributor1.firstCommitDate.compareTo(contributor2.firstCommitDate);
        }
    };

    protected final static Comparator<Contributor> NAME_COMPARATOR = new Comparator<Contributor>() {
        public int compare(Contributor contributor1, Contributor contributor2) {
            return contributor1.name.compareTo(contributor2.name);
        }
    };

    /**
     * The string to prepend to every contributor name
     *
     * @parameter property="mavanagaiata.contributors.contributorPrefix"
     */
    protected String contributorPrefix = " * ";

    /**
     * The header to print above the changelog
     *
     * @parameter property="mavanagaiata.contributors.header"
     */
    protected String header = "Contributors\n============\n";

    /**
     * The file to write the contributors list to
     *
     * @parameter property="mavanagaiata.contributors.outputFile"
     * @since 0.4.1
     */
    protected File outputFile;

    /**
     * Whether the number of contributions should be listed
     *
     * @parameter property="mavanagaiata.contributors.showCounts"
     */
    protected boolean showCounts = true;

    /**
     * Whether the email addresses of contributors should be listed
     *
     * @parameter property="mavanagaiata.contributors.showEmail"
     */
    protected boolean showEmail = false;

    /**
     * The method used to sort contributors
     *
     * @parameter property="mavanagaiata.contributors.sort"
     */
    protected String sort;

    /**
     * {@inheritDoc}
     * @see #initSort
     */
    @Override
    public boolean init() throws MojoExecutionException {
        this.initSort();

        return super.init();
    }

    /**
     * Selects the attribute to use for sorting contributors
     */
    protected void initSort() {
        if (this.sort == null) {
            this.sort = "count";
        } else {
            this.sort = this.sort.toLowerCase();
            if (!this.sort.equals("date") && !this.sort.equals("name")) {
                this.sort = "count";
            }
        }
    }

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a list of contributors from the authors of the
     * commits.
     *
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void run() throws MojoExecutionException {
        this.contributorPrefix = this.contributorPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header            = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");

        try {
            ContributorsWalkAction walkAction = new ContributorsWalkAction();
            this.repository.walkCommits(walkAction);

            ArrayList<Contributor> contributors = new ArrayList<Contributor>(walkAction.contributors.values());
            if (sort.equals("date")) {
                Collections.sort(contributors, DATE_COMPARATOR);
            } else if (sort.equals("name")) {
                Collections.sort(contributors, NAME_COMPARATOR);
            } else {
                Collections.sort(contributors, COUNT_COMPARATOR);
            }

            this.outputStream.println(this.header);

            for (Contributor contributor : contributors) {
                this.outputStream.print(this.contributorPrefix + contributor.name);
                if (this.showEmail) {
                    this.outputStream.print(" (" + contributor.emailAddress + ")");
                }
                if (this.showCounts) {
                    this.outputStream.print(" (" + contributor.count + ")");
                }
                this.outputStream.println();
            }

            this.insertFooter();
        } catch (GitRepositoryException e) {
            throw new MojoExecutionException("Unable to read contributors from Git", e);
        }
    }

    /**
     * Returns the output file for the generated contributors list
     *
     * @return The output file for the generated contributors list
     */
    public File getOutputFile() {
        return this.outputFile;
    }

    /**
     * Sets the output file for the generated contributors list
     *
     * @param outputFile The output file for the generated contributors list
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    class ContributorsWalkAction extends CommitWalkAction {

        protected HashMap<String, Contributor> contributors;

        public ContributorsWalkAction() {
            this.contributors = new HashMap<String, Contributor>();
        }

        protected void run() throws GitRepositoryException {
            String emailAddress = this.currentCommit.getAuthorEmailAddress();
            Contributor contributor = this.contributors.get(emailAddress);
            if (contributor == null) {
                this.contributors.put(emailAddress, new Contributor(this.currentCommit));
            } else {
                contributor.addCommit(this.currentCommit);
            }
        }
    }

    class Contributor {

        Integer count = 1;

        String emailAddress;

        Date firstCommitDate;

        String name;

        public Contributor(GitCommit commit) {
            this.emailAddress    = commit.getAuthorEmailAddress();
            this.firstCommitDate = commit.getAuthorDate();
            this.name            = commit.getAuthorName();
        }

        void addCommit(GitCommit commit) {
            this.count ++;

            if (commit.getAuthorDate().before(this.firstCommitDate)) {
                this.firstCommitDate = commit.getAuthorDate();
            }
        }

    }

}
