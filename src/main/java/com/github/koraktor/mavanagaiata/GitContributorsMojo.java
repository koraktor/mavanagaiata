/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
 * @since 0.2.0
 */
public class GitContributorsMojo extends AbstractGitOutputMojo {

    /**
     * The string to prepend to every contributor name
     *
     * @parameter expression="${mavanagaiata.contributors.contributorPrefix}"
     */
    protected String contributorPrefix = " * ";

    /**
     * The header to print above the changelog
     *
     * @parameter expression="${mavanagaiata.contributors.header}"
     */
    protected String header = "Contributors\n============\n";

    /**
     * Whether the number of contributoions should be listed
     *
     * @parameter expression="${mavanagaiata.contributors.showCounts}"
     */
    protected boolean showCounts = true;

    /**
     * Whether the email addresses of contributors should be listed
     *
     * @parameter expression="${mavanagaiata.contributors.showEmail}"
     */
    protected boolean showEmail = false;

    /**
     * The method used to sort contributors
     *
     * @parameter expression="${mavanagaiata.contributors.sort}"
     */
    protected String sort;

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
            this.initOutputStream();

            RevWalk revWalk = new RevWalk(this.repository);
            revWalk.markStart(this.getHead());

            RevCommit tempCommit;
            List<RevCommit> commits = new ArrayList<RevCommit>();
            while((tempCommit = revWalk.next()) != null) {
                commits.add(tempCommit);
            }

            if(this.sort == null) {
                this.sort = "count";
            } else {
                this.sort = this.sort.toLowerCase();
                if (!this.sort.equals("date") && !this.sort.equals("name")) {
                    this.sort = "count";
                }
            }

            final Map<String, Integer> counts = new HashMap<String, Integer>();
            if(this.showCounts || this.sort.equals("count")) {
                for(RevCommit commit : commits) {
                    String emailAddress = commit.getAuthorIdent().getEmailAddress();
                    if(!counts.containsKey(emailAddress)) {
                        counts.put(emailAddress, 1);
                    } else {
                        counts.put(emailAddress, counts.get(emailAddress) + 1);
                    }
                }
            }

            if(this.sort.equals("date")) {
                Collections.reverse(commits);
            } else if(this.sort.equals("name")) {
                Collections.sort(commits, new Comparator<RevCommit>() {
                    public int compare(RevCommit c1, RevCommit c2) {
                        String a1 = c1.getAuthorIdent().getName();
                        String a2 = c2.getAuthorIdent().getName();
                        return a1.compareTo(a2);
                    }
                });
            } else {
                Collections.sort(commits, new Comparator<RevCommit>() {
                    public int compare(RevCommit c1, RevCommit c2) {
                        Integer count1 = counts.get(c1.getAuthorIdent().getEmailAddress());
                        Integer count2 = counts.get(c1.getAuthorIdent().getEmailAddress());
                        return count1.compareTo(count2);
                    }
                });
            }

            final LinkedHashMap<String, String> contributors = new LinkedHashMap<String, String>();
            for(RevCommit commit : commits) {
                PersonIdent author = commit.getAuthorIdent();
                String emailAddress = author.getEmailAddress();
                if(!contributors.containsKey(emailAddress)) {
                    contributors.put(emailAddress, author.getName());
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
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read contributors from Git", e);
        } finally {
            this.closeOutputStream();
        }
    }

    /**
     * Sets the output file for the generated changelog
     *
     * @param outputFile The output file for the generated changelog
     * @parameter expression="${mavanagaiata.contributors.outputFile}"
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
