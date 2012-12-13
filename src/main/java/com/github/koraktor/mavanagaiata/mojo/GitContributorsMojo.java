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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    protected final static Comparator<GitCommit> NAME_COMPARATOR = new Comparator<GitCommit>() {
        @Override
        public int compare(GitCommit commit1, GitCommit commit2) {
            return commit1.getAuthorName().compareTo(commit2.getAuthorName());
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
    public void init() throws MojoExecutionException {
        this.initSort();

        super.init();
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

            final Map<String, Integer> counts = new HashMap<String, Integer>();
            if(this.showCounts || this.sort.equals("count")) {
                for(GitCommit commit : walkAction.commits) {
                    String emailAddress = commit.getAuthorEmailAddress();
                    if(!counts.containsKey(emailAddress)) {
                        counts.put(emailAddress, 1);
                    } else {
                        counts.put(emailAddress, counts.get(emailAddress) + 1);
                    }
                }
            }

            if(this.sort.equals("date")) {
                Collections.reverse(walkAction.commits);
            } else if(this.sort.equals("name")) {
                Collections.sort(walkAction.commits, new Comparator<GitCommit>() {
                    public int compare(GitCommit c1, GitCommit c2) {
                        String a1 = c1.getAuthorName();
                        String a2 = c2.getAuthorName();
                        return a1.compareTo(a2);
                    }
                });
            } else {
                Collections.sort(walkAction.commits, new Comparator<GitCommit>() {
                    public int compare(GitCommit c1, GitCommit c2) {
                        Integer count1 = counts.get(c1.getAuthorEmailAddress());
                        Integer count2 = counts.get(c1.getAuthorEmailAddress());
                        return count1.compareTo(count2);
                    }
                });
            }

            final LinkedHashMap<String, String> contributors = new LinkedHashMap<String, String>();
            for(GitCommit commit : walkAction.commits) {
                String emailAddress = commit.getAuthorEmailAddress();
                if(!contributors.containsKey(emailAddress)) {
                    contributors.put(emailAddress, commit.getAuthorName());
                }
            }

            List<String> emailAddresses = new ArrayList<String>(contributors.keySet());

            if (!this.sort.equals("date")) {
                if(this.sort.equals("name")) {
                    Collections.sort(emailAddresses, new Comparator<String>() {
                        public int compare(String e1, String e2) {
                            String n1 = contributors.get(e1);
                            String n2 = contributors.get(e2);
                            return n1.compareTo(n2);
                        }
                    });
                } else {
                    Collections.sort(emailAddresses, new Comparator<String>() {
                        public int compare(String e1, String e2) {
                            Integer count1 = counts.get(e1);
                            Integer count2 = counts.get(e2);
                            return count2.compareTo(count1);
                        }
                    });
                }
            }

            this.outputStream.println(this.header);

            for(String emailAddress : emailAddresses) {
                this.outputStream.print(this.contributorPrefix + contributors.get(emailAddress));
                if(this.showEmail) {
                    this.outputStream.print(" (" + emailAddress + ")");
                }
                if(this.showCounts) {
                    this.outputStream.print(" (" + counts.get(emailAddress) + ")");
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

        protected List<GitCommit> commits;

        public ContributorsWalkAction() {
            this.commits = new ArrayList<GitCommit>();
        }

        protected void run() throws GitRepositoryException {
            this.commits.add(this.currentCommit);
        }
    }

}
