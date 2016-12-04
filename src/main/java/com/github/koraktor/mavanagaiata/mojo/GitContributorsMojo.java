/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.MailMap;

/**
 * This goal allows to generate a list of contributors for the currently
 * checked out branch of the Git repository. It will list all authors of the
 * commits in this branch. It can be configured to display the changelog or
 * save it to a file.
 *
 * @author Sebastian Staudt
 * @since 0.2.0
 */
@Mojo(name ="contributors",
      defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
      threadSafe = true)
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
     */
    @Parameter(property = "mavanagaiata.contributors.contributorPrefix",
               defaultValue = " * ")
    protected String contributorPrefix;

    /**
     * The header to print above the changelog
     */
    @Parameter(property = "mavanagaiata.contributors.header",
               defaultValue = "Contributors\n============\n")
    protected String header;

    protected MailMap mailMap;

    /**
     * The file to write the contributors list to
     */
    @Parameter(property = "mavanagaiata.contributors.outputFile")
    protected File outputFile;

    /**
     * Whether the number of contributions should be listed
     */
    @Parameter(property = "mavanagaiata.contributors.showCounts",
               defaultValue = "true")
    protected boolean showCounts;

    /**
     * Whether the email addresses of contributors should be listed
     */
    @Parameter(property = "mavanagaiata.contributors.showEmail",
               defaultValue = "false")
    protected boolean showEmail;

    /**
     * The method used to sort contributors
     * <p>
     * Available values are {@code count}, {@code date} and {@code name}.
     */
    @Parameter(property = "mavanagaiata.contributors.sort",
               defaultValue = "count")
    protected String sort;

    @Override
    public GitRepository init() throws MavanagaiataMojoException {
        this.initConfiguration();

        return super.init();
    }

    /**
     * Selects the attribute to use for sorting contributors
     */
    protected void initConfiguration() {
        this.contributorPrefix = this.contributorPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header            = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");

        if (this.sort == null) {
            this.sort = "count";
        } else {
            this.sort = this.sort.toLowerCase();
            if (!this.sort.equals("date") && !this.sort.equals("name")) {
                this.sort = "count";
            }
        }

        super.initConfiguration();
    }

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a list of contributors from the authors of the
     * commits.
     *
     * @throws MavanagaiataMojoException if retrieving information from the Git
     *         repository fails
     */
    @Override
    protected void writeOutput(GitRepository repository, PrintStream printStream)
            throws MavanagaiataMojoException {
        try {
            mailMap = repository.getMailMap();

            ContributorsWalkAction result = repository.walkCommits(new ContributorsWalkAction());

            ArrayList<Contributor> contributors = new ArrayList<>(result.contributors.values());
            switch (sort) {
                case "date":
                    Collections.sort(contributors, DATE_COMPARATOR);
                    break;
                case "name":
                    Collections.sort(contributors, NAME_COMPARATOR);
                    break;
                default:
                    Collections.sort(contributors, COUNT_COMPARATOR);
            }

            printStream.println(this.header);

            for (Contributor contributor : contributors) {
                printStream.print(this.contributorPrefix + contributor.name);
                if (this.showEmail) {
                    printStream.print(" (" + contributor.emailAddress + ")");
                }
                if (this.showCounts) {
                    printStream.print(" (" + contributor.count + ")");
                }
                printStream.println();
            }
        } catch (GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to read contributors from Git", e);
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
            this.contributors = new HashMap<>();
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

            if (GitContributorsMojo.this.mailMap.exists()) {
                this.emailAddress = GitContributorsMojo.this.mailMap.getCanonicalAuthorEmailAddress(commit);
                this.name = GitContributorsMojo.this.mailMap.getCanonicalAuthorName(commit);
            }
        }

        void addCommit(GitCommit commit) {
            this.count ++;

            if (commit.getAuthorDate().before(this.firstCommitDate)) {
                this.firstCommitDate = commit.getAuthorDate();
            }
        }

    }

}
