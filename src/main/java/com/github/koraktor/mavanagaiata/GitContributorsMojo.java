/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * This goal allows to generate a list of contributors for the currently
 * checked out branch of the Git repository. It will list all authors of the
 * commits in this branch. It can be configured to display the changelog or
 * save it to a file.
 *
 * @author Sebastian Staudt
 * @goal contributors
 * @phase compile
 * @requiresProject
 */
public class GitContributorsMojo extends AbstractGitMojo {

    /**
     * The header to print above the changelog
     *
     * @parameter expression="${mavanagaiata.contributors.header}"
     */
    protected String header = "Contributors\n============\n";

    /**
     * The string to prepend to every contributor name
     *
     * @parameter expression="${mavanagaiata.contributors.contributorPrefix}"
     */
    protected String contributorPrefix = " * ";

    /**
     * The file to write the contributors to
     *
     * @parameter expression="${mavanagaiata.contributors.outputFile}"
     */
    protected File outputFile;

    /**
     * Whether the email addresses of contributors should be listed
     *
     * @parameter expression="${mavanagaiata.contributors.showEmail}"
     */
    protected boolean showEmail = false;

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a list of contributors from the authors of the
     * commits.
     *
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void execute() throws MojoExecutionException {
        this.contributorPrefix = this.contributorPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header            = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");

        try {
            this.initRepository();

            RevWalk revWalk = new RevWalk(this.repository);
            revWalk.markStart(this.getHead());

            PrintStream outputStream;
            if(this.outputFile == null) {
                outputStream = System.out;
            } else {
                outputStream = new PrintStream(this.outputFile);
            }

            Map<String, String> contributors = new HashMap<String, String>();

            RevCommit commit;
            while((commit = revWalk.next()) != null) {
                PersonIdent author = commit.getAuthorIdent();
                String emailAddress = author.getEmailAddress();
                if(!contributors.containsKey(emailAddress)) {
                    contributors.put(emailAddress, author.getName());
                }
            }

            outputStream.println(this.header);
            for(Map.Entry<String, String> contributor : contributors.entrySet()) {
                outputStream.print(this.contributorPrefix + contributor.getValue());
                if(this.showEmail) {
                    outputStream.print(" (" + contributor.getKey() + ")");
                }
                outputStream.println();
            }

            outputStream.flush();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read contributors from Git", e);
        }
    }
}
