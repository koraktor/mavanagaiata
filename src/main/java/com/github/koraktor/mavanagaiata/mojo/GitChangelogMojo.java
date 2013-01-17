/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.text.SimpleDateFormat;

import org.apache.maven.plugin.MojoExecutionException;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;

/**
 * This goal allows to generate a changelog of the currently checked out branch
 * of the Git repository. It will use information from tags and commit messages
 * to build a reverse chronological summary of the development. It can be
 * configured to display the changelog or save it to a file.
 *
 * @author Sebastian Staudt
 * @goal changelog
 * @phase process-resources
 * @requiresProject
 * @since 0.2.0
 */
public class GitChangelogMojo extends AbstractGitOutputMojo {

    /**
     * Whether to create links to GitHub's compare view
     *
     * @parameter property="mavanagaiata.changelog.gitHubLinks"
     */
    protected boolean createGitHubLinks = false;

    /**
     * The string to prepend to every commit message
     *
     * @parameter property="mavanagaiata.changelog.commitPrefix"
     *            default-value=" * "
     */
    protected String commitPrefix;

    /**
     * The project name for GitHub links
     *
     * @parameter property="mavanagaiata.changelog.gitHubProject"
     */
    protected String gitHubProject;

    /**
     * The user name for GitHub links
     *
     * @parameter property="mavanagaiata.changelog.gitHubUser"
     */
    protected String gitHubUser;

    /**
     * The header to print above the changelog
     *
     * @parameter property="mavanagaiata.changelog.header"
     *            default-value="Changelog\n=========\n"
     */
    protected String header;

    /**
     * The file to write the changelog to
     *
     * @parameter property="mavanagaiata.changelog.outputFile"
     * @since 0.4.1
     */
    protected File outputFile;

    /**
     * Whether to skip tagged commits' messages
     *
     * This is useful when usually tagging commits like "Version bump to X.Y.Z"
     *
     * @parameter property="mavanagaiata.changelog.skipTagged"
     *            default-value="false"
     */
    protected boolean skipTagged;

    /**
     * The string to prepend to the tag name
     *
     * @parameter property="mavanagaiata.changelog.tagPrefix"
     *            default-value="\nVersion "
     */
    protected String tagPrefix;

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a changelog from the commits contained in that
     * branch.
     *
     * @throws MojoExecutionException if retrieving information from the Git
     *         repository fails
     */
    public void run() throws MojoExecutionException {
        try {
            this.outputStream.println(this.header);

            ChangelogWalkAction walkAction = new ChangelogWalkAction();
            this.repository.walkCommits(walkAction);

            if (this.createGitHubLinks) {
                if (walkAction.getCurrentTag() == null) {
                    this.insertGitHubLink(this.repository.getBranch(), null, true);
                } else {
                    this.insertGitHubLink(walkAction.getCurrentTag(), (GitTag) null);
                }
            }

            this.insertFooter();
        } catch (GitRepositoryException e) {
            throw new MojoExecutionException("Unable to generate changelog from Git", e);
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
     * @throws MojoExecutionException if an error occurs while accessing the
     *         Git repository or the changelog file
     */
    protected boolean init() throws MojoExecutionException {
        this.initConfiguration();

        return super.init();
    }

    protected void initConfiguration() {
        this.commitPrefix = this.commitPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header       = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.tagPrefix    = this.tagPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");

        if (this.gitHubProject == null || this.gitHubProject.length() == 0 ||
            this.gitHubUser == null || this.gitHubUser.length() == 0) {
            this.createGitHubLinks = false;
        }
    }

    protected void insertGitHubLink(GitTag lastTag, String branch) {
        this.insertGitHubLink(lastTag.getName(), branch, true);
    }

    protected void insertGitHubLink(GitTag lastTag, GitTag currentTag) {
        String tagName = (currentTag == null) ? null : currentTag.getName();
        this.insertGitHubLink(lastTag.getName(), tagName, false);
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
    protected void insertGitHubLink(String lastRef, String currentRef, boolean isBranch) {
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

    /**
     * Sets the output file for the generated changelog
     *
     * @param outputFile The output file for the generated changelog
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    class ChangelogWalkAction extends CommitWalkAction {

        private GitTag currentTag;

        private SimpleDateFormat dateFormatter;

        private boolean firstCommit = true;

        private GitTag lastTag;

        public ChangelogWalkAction() {
            this.dateFormatter = new SimpleDateFormat(dateFormat);
        }

        public GitTag getCurrentTag() {
            return this.currentTag;
        }

        protected void run() throws GitRepositoryException {
            if (repository.getTags().containsKey(this.currentCommit.getId())) {
                this.lastTag = this.currentTag;
                this.currentTag = repository.getTags().get(this.currentCommit.getId());
                if (createGitHubLinks) {
                    if (this.lastTag == null) {
                        insertGitHubLink(this.currentTag, repository.getBranch());
                    } else {
                        insertGitHubLink(this.currentTag, this.lastTag);
                    }
                }

                this.dateFormatter.setTimeZone(this.currentTag.getTimeZone());
                String dateString = this.dateFormatter.format(this.currentTag.getDate());

                if (this.firstCommit && tagPrefix.startsWith("\n")) {
                    outputStream.print(tagPrefix.substring(1));
                } else {
                    outputStream.print(tagPrefix);
                }
                outputStream.println(currentTag.getName() + " - " + dateString + "\n");

                if (skipTagged) {
                    this.firstCommit = false;
                    return;
                }
            } else if (this.firstCommit) {
                outputStream.println("Commits on branch \"" + repository.getBranch() + "\"\n");
            }

            outputStream.println(commitPrefix + this.currentCommit.getMessageSubject());
            this.firstCommit = false;
        }

    }

}
