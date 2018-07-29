/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
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

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

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
public class ContributorsMojo extends AbstractGitOutputMojo {

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

    /**
     * Selects the attribute to use for sorting contributors
     */
    protected void initConfiguration() {
        contributorPrefix = unescapeFormatNewlines(contributorPrefix);
        header            = unescapeFormatNewlines(header);

        if (!equalsAnyIgnoreCase(sort, "date", "name")) {
            sort = "count";
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
    protected void writeOutput(GitRepository repository)
            throws MavanagaiataMojoException {
        try {
            mailMap = repository.getMailMap();

            ContributorsWalkAction result = repository.walkCommits(new ContributorsWalkAction());

            ArrayList<Contributor> contributors = new ArrayList<>(result.contributors.values());
            switch (sort) {
                case "date":
                    contributors.sort(comparing(Contributor::getFirstCommitDate));
                    break;
                case "name":
                    contributors.sort(comparing(Contributor::getName));
                    break;
                default:
                    contributors.sort(comparing(Contributor::getCount).reversed());
            }

            printStream.println(header);

            for (Contributor contributor : contributors) {
                printStream.print(contributorPrefix + contributor.name);
                if (showEmail) {
                    printStream.print(" (" + contributor.emailAddress + ")");
                }
                if (showCounts) {
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
        return outputFile;
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

        HashMap<String, Contributor> contributors = new HashMap<>();

        protected void run() {
            String emailAddress = currentCommit.getAuthorEmailAddress();
            Contributor contributor = contributors.get(emailAddress);
            if (contributor == null) {
                contributors.put(emailAddress, new Contributor(currentCommit));
            } else {
                contributor.addCommit(currentCommit);
            }
        }
    }

    class Contributor {

        Integer count = 1;
        String emailAddress;
        Date firstCommitDate;
        String name;

        Contributor(GitCommit commit) {
            firstCommitDate = commit.getAuthorDate();

            if (ContributorsMojo.this.mailMap.exists()) {
                emailAddress = ContributorsMojo.this.mailMap.getCanonicalAuthorEmailAddress(commit);
                name = ContributorsMojo.this.mailMap.getCanonicalAuthorName(commit);
            } else {
                emailAddress = commit.getAuthorEmailAddress();
                name = commit.getAuthorName();
            }
        }

        void addCommit(GitCommit commit) {
            count ++;

            if (commit.getAuthorDate().before(firstCommitDate)) {
                firstCommitDate = commit.getAuthorDate();
            }
        }

        Integer getCount() {
            return count;
        }

        Date getFirstCommitDate() {
            return firstCommitDate;
        }

        String getName() {
            return name;
        }
    }

}
