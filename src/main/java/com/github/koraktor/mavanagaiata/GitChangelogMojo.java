/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * This goal allows to generate a changelog of the currently checked out branch
 * of the Git repository. It will use information from tags and commit messages
 * to build a reverse chronological summary of the development. It can be
 * configured to display the changelog or save it to a file.
 *
 * @author Sebastian Staudt
 * @goal changelog
 * @phase compile
 * @requiresProject
 * @since 0.2.0
 */
public class GitChangelogMojo extends AbstractGitOutputMojo {

    /**
     * Whether to create links to GitHub's compare view
     *
     * @parameter expression="${mavanagaiata.changelog.gitHubLinks}"
     */
    protected boolean createGitHubLinks = false;

    /**
     * The string to prepend to every commit message
     *
     * @parameter expression="${mavanagaiata.changelog.commitPrefix}"
     */
    protected String commitPrefix = " * ";

    /**
     * The date format to use for tag output
     *
     * @parameter expression="${mavanagaiata.changelog.dateFormat}"
     */
    protected String dateFormat = baseDateFormat;

    /**
     * The project name for GitHub links
     *
     * @parameter expression="${mavanagaiata.changelog.gitHubProject}"
     */
    protected String gitHubProject;

    /**
     * The user name for GitHub links
     *
     * @parameter expression="${mavanagaiata.changelog.gitHubUser}"
     */
    protected String gitHubUser;

    /**
     * The header to print above the changelog
     *
     * @parameter expression="${mavanagaiata.changelog.header}"
     */
    protected String header = "Changelog\n=========\n";

    /**
     * The file to write the changelog to
     *
     * @parameter property="mavanagaiata.changelog.outputFile"
     */
    protected File outputFile;

    /**
     * Whether to skip tagged commits' messages
     *
     * This is useful when usually tagging commits like "Version bump to X.Y.Z"
     *
     * @parameter expression="${mavanagaiata.changelog.skipTagged}"
     */
    protected boolean skipTagged = false;

    /**
     * The string to prepend to the tag name
     *
     * @parameter expression="${mavanagaiata.changelog.tagPrefix}"
     */
    protected String tagPrefix = "\nVersion ";

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a changelog from the commits contained in that
     * branch.
     *
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void execute() throws MojoExecutionException {
        try {
            this.init();

            RevWalk revWalk = new RevWalk(this.repository);
            Map<String, Ref> tagRefs = this.repository.getTags();
            Map<String, RevTag> tags = new HashMap<String, RevTag>();

            for(Map.Entry<String, Ref> tag : tagRefs.entrySet()) {
                try {
                    RevTag revTag = revWalk.parseTag(tag.getValue().getObjectId());
                    RevObject object = revWalk.peel(revTag);
                    if(!(object instanceof RevCommit)) {
                        continue;
                    }
                    tags.put(object.getName(), revTag);
                } catch(IncorrectObjectTypeException e) {
                    continue;
                }
            }

            revWalk.markStart(this.getHead());

            this.outputStream.println(this.header);

            String branch = this.repository.getBranch();
            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            RevCommit commit;
            RevTag currentTag = null;
            RevTag lastTag;
            boolean firstCommit = true;
            while((commit = revWalk.next()) != null) {
                if(tags.containsKey(commit.getName())) {
                    lastTag = currentTag;
                    currentTag = tags.get(commit.getName());
                    if(lastTag == null) {
                        this.insertGitHubLink(currentTag, branch);
                    } else {
                        this.insertGitHubLink(currentTag, lastTag);
                    }

                    PersonIdent taggerIdent = currentTag.getTaggerIdent();
                    dateFormat.setTimeZone(taggerIdent.getTimeZone());
                    String dateString = dateFormat.format(taggerIdent.getWhen());
                    this.outputStream.println(this.tagPrefix + currentTag.getTagName() + " - " + dateString + "\n");

                    if(this.skipTagged) {
                        firstCommit = false;
                        continue;
                    }
                } else if(firstCommit) {
                    this.outputStream.println("Commits on branch \"" + branch + "\"\n");
                }

                this.outputStream.println(this.commitPrefix + commit.getShortMessage());
                firstCommit = false;
            }

            if(currentTag == null) {
                this.insertGitHubLink(branch, null);
            } else {
                this.insertGitHubLink(currentTag, null);
            }

            this.insertFooter();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to generate changelog from Git", e);
        } finally {
            this.cleanup();
        }
    }

    /**
     * Returns the output file for the generated changelog
     *
     * @return The output file for the generated changelog
     */
    public File getOutputFile() {
        return this.outputFile;
    }

    /**
     * Initializes this mojo
     *
     * @throws IOException if an error occurs while accessing the Git
     *         repository or the changelog file
     */
    protected void init() throws IOException, MojoExecutionException {
        this.commitPrefix = this.commitPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header       = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.tagPrefix    = this.tagPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");

        if(this.gitHubProject == null || this.gitHubProject.length() == 0 ||
           this.gitHubUser == null || this.gitHubUser.length() == 0) {
            this.createGitHubLinks = false;
        }

        this.initRepository();
        this.initOutputStream();
    }

    /**
     * Generates a link to the GitHub compare / commits view and inserts it
     * into the changelog
     * <p>
     * If no current ref is provided, the generated text will link to the
     * commits view, listing all commits of the latest tag or the whole branch.
     * Otherwise the text will link to the compare view, listing all commits
     * that are in the current ref, but not in the last one.
     *
     * @param lastRef The last tag or branch in the changelog
     * @param currentRef The current tag or branch in the changelog
     */
    private void insertGitHubLink(Object lastRef, Object currentRef) {
        if(this.createGitHubLinks) {
            boolean isBranch;
            if(currentRef == null) {
                isBranch = lastRef instanceof String;
            } else {
                isBranch = currentRef instanceof String;
                if(!isBranch) {
                    currentRef = ((RevTag) currentRef).getTagName();
                }
            }

            if(lastRef instanceof RevTag) {
                lastRef = ((RevTag) lastRef).getTagName();
            }

            String url = String.format("https://github.com/%s/%s/",
                this.gitHubUser,
                this.gitHubProject);
            if(currentRef == null) {
                url += String.format("commits/%s", lastRef);
            } else {
                url += String.format("compare/%s...%s", lastRef, currentRef);
            }

            String text = "See Git history for ";
            if(currentRef != null) {
                if(isBranch) {
                    text += "changes in the \"" + currentRef +
                            "\" branch since version " + lastRef;
                } else {
                    text += "version " + currentRef;
                }
            } else {
                if(isBranch) {
                    text += "changes in the \"" + lastRef + "\" branch";
                } else {
                    text += "version " + lastRef;
                }
            }
            text += " at: ";

            this.outputStream.println("\n" + text + url);
        }
    }

    /**
     * Sets the output file for the generated changelog
     *
     * @param outputFile The output file for the generated changelog
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

}
