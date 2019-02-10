/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2019, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.MailMap;

import static java.util.Comparator.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.text.StringEscapeUtils.*;

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

    private static final Map<CharSequence, CharSequence> MARKDOWN_TRANSLATION_MAP = new HashMap<>();
    static {
        MARKDOWN_TRANSLATION_MAP.put("[", "\\[");
        MARKDOWN_TRANSLATION_MAP.put("]", "\\]");
    }

    private static final CharSequenceTranslator MARKDOWN_TRANSLATOR = new LookupTranslator(MARKDOWN_TRANSLATION_MAP);

    /**
     * The string to prepend to every contributor name
     */
    @Parameter(property = "mavanagaiata.contributors.contributorPrefix",
               defaultValue = " * ")
    String contributorPrefix;

    @Parameter(property = "mavanagaiata.contributors.escapeHtml",
               defaultValue = "false")
    boolean escapeHtml;

    @Parameter(property = "mavanagaiata.contributors.escapeMarkdown",
               defaultValue = "false")
    boolean escapeMarkdown;

    /**
     * The header to print above the changelog
     */
    @Parameter(property = "mavanagaiata.contributors.header",
               defaultValue = "Contributors\n============\n")
    String header;

    private MailMap mailMap;

    /**
     * The file to write the contributors list to
     */
    @Parameter(property = "mavanagaiata.contributors.outputFile")
    File outputFile;

    /**
     * Whether the number of contributions should be listed
     */
    @Parameter(property = "mavanagaiata.contributors.showCounts",
               defaultValue = "true")
    boolean showCounts;

    /**
     * Whether the email addresses of contributors should be listed
     */
    @Parameter(property = "mavanagaiata.contributors.showEmail",
               defaultValue = "false")
    boolean showEmail;

    /**
     * The method used to sort contributors
     * <p>
     * Available values are {@code count}, {@code date} and {@code name}.
     */
    @Parameter(property = "mavanagaiata.contributors.sort",
               defaultValue = "count")
    String sort;

    /**
     * Selects the attribute to use for sorting contributors
     */
    @Override
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
                printStream.print(contributorPrefix + escapeName(contributor.name));
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
     * Returns an escaped form of the contributor name
     * <p>
     * Depending on the {@link #escapeHtml} and {@link #escapeMarkdown} fields
     * this methods escapes HTML tags and/or Markdown link brackets.
     *
     * @param name The name of the contributor
     * @return An escaped form of the contributor
     */
    private String escapeName(String name) {
        if (escapeHtml) {
            name = escapeHtml4(name);
        }

        if (escapeMarkdown) {
            name = MARKDOWN_TRANSLATOR.translate(name);
        }

        return name;
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
