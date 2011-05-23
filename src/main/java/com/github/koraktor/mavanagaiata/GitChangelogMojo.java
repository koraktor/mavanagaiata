/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

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
     * The date format to use for tag output
     *
     * @parameter expression="${mavanagaiata.changelog.dateFormat}"
     */
    protected String dateFormat = baseDateFormat;

    /**
     * The header to print above the changelog
     *
     * @parameter expression="${mavanagaiata.changelog.header}"
     */
    protected String header = "Changelog\n=========\n";

    /**
     * The string to prepend to every commit message
     *
     * @parameter expression="${mavanagaiata.changelog.commitPrefix}"
     */
    protected String commitPrefix = " * ";

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
        this.commitPrefix = this.commitPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header       = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.tagPrefix    = this.tagPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");

        try {
            this.initRepository();
            this.initOutputStream();

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

            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            RevCommit commit;
            RevTag tag = null;
            while((commit = revWalk.next()) != null) {
                if(tags.containsKey(commit.getName())) {
                    tag = tags.get(commit.getName());
                    PersonIdent taggerIdent = tag.getTaggerIdent();
                    dateFormat.setTimeZone(taggerIdent.getTimeZone());
                    String dateString = dateFormat.format(taggerIdent.getWhen());
                    this.outputStream.println(this.tagPrefix + tag.getTagName() + " - " + dateString + "\n");

                    if(this.skipTagged) {
                        continue;
                    }
                } else if(tag == null) {
                    String branch = this.repository.getBranch();
                    this.outputStream.println("Commits on branch \"" + branch + "\"\n");
                    tag = tags.values().iterator().next();
                }

                this.outputStream.println(this.commitPrefix + commit.getShortMessage());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to generate changelog from Git", e);
        } finally {
            this.closeOutputStream();
        }
    }
}
