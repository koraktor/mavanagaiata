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
public class GitChangelogMojo extends AbstractGitMojo {

    /**
     * The date format to use for tag output
     *
     * @parameter expression="${mavanagaiata.changelog.dateFormat}"
     */
    protected String dateFormat = "MM/dd/yyyy hh:mm a Z";

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
     * The file to write the changelog to
     *
     * @parameter expression="${mavanagaiata.changelog.outputFile}"
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
        this.commitPrefix = this.commitPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.header       = this.header.replaceAll("([^\\\\])\\\\n", "$1\n");
        this.tagPrefix    = this.tagPrefix.replaceAll("([^\\\\])\\\\n", "$1\n");

        try {
            this.initRepository();

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

            PrintStream outputStream;
            if(this.outputFile == null) {
                outputStream = System.out;
            } else {
                if(!this.outputFile.getParentFile().exists()) {
                    this.outputFile.getParentFile().mkdirs();
                }
                outputStream = new PrintStream(this.outputFile);
            }

            outputStream.println(this.header);

            SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
            RevCommit commit;
            while((commit = revWalk.next()) != null) {
                if(tags.containsKey(commit.getName())) {
                    RevTag tag = tags.get(commit.getName());
                    PersonIdent taggerIdent = tag.getTaggerIdent();
                    dateFormat.setTimeZone(taggerIdent.getTimeZone());
                    String dateString = dateFormat.format(taggerIdent.getWhen());
                    outputStream.println(this.tagPrefix + tag.getTagName() + " - " + dateString + "\n");

                    if(this.skipTagged) {
                        continue;
                    }
                }

                outputStream.println(this.commitPrefix + commit.getShortMessage());
            }

            outputStream.flush();
            if(this.outputFile != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to generate changelog from Git", e);
        }
    }
}
