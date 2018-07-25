/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import static com.github.koraktor.mavanagaiata.mojo.AbstractGitOutputMojo.unescapeFormatNewlines;

public class ChangelogFormat {

    /**
     * The format for the branch line
     */
    String branch = "Commits on branch \"%s\"\n";

    /**
     * The format for the link to the history from the last tag to the current
     * branch on GitHub
     */
    String branchLink = "\nSee Git history for changes in the \"%s\" branch since version %s at: %s";

    /**
     * The format for the link to the branch history on GitHub
     */
    String branchOnlyLink = "\nSee Git history for changes in the \"%s\" branch at: %s";

    /**
     * The string to prepend to every commit message
     */
    String commitPrefix = " * ";

    /**
     * Whether to create links to GitHub's compare view
     */
    boolean createLinks = true;

    /**
     * The header to print above the changelog
     */
    String header = "Changelog\n=========\n";

    /**
     * The format for a tag line
     */
    String tag = "\nVersion %s – %s\n";

    /**
     * The format for the link to the tag history on GitHub
     */
    String tagLink = "\nSee Git history for version %s at: %s";

    /**
     * Prepare the format strings for use
     */
    void prepare() {
        branch = unescapeFormatNewlines(branch);
        branchLink = unescapeFormatNewlines(branchLink);
        branchOnlyLink = unescapeFormatNewlines(branchOnlyLink);
        commitPrefix = unescapeFormatNewlines(commitPrefix);
        header = unescapeFormatNewlines(header);
        tag = unescapeFormatNewlines(tag);
        tagLink = unescapeFormatNewlines(tagLink);
    }

}
