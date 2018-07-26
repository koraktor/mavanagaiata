/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.util.Optional;

import static com.github.koraktor.mavanagaiata.mojo.AbstractGitOutputMojo.unescapeFormatNewlines;

public class ChangelogFormat {

    public enum Formats {
        DEFAULT(new ChangelogDefaultFormat()),
        MARKDOWN(new ChangelogMarkdownFormat());

        private ChangelogFormat format;

        Formats(ChangelogFormat format) {
            this.format = format;
        }

        ChangelogFormat getFormat() {
            return format;
        }
    }

    /**
     * The format for the branch line
     */
    String branch;

    /**
     * The format for the link to the history from the last tag to the current
     * branch on GitHub
     */
    String branchLink;

    /**
     * The format for the link to the branch history on GitHub
     */
    String branchOnlyLink;

    /**
     * The string to prepend to every commit message
     */
    String commitPrefix;

    /**
     * Whether to create links to GitHub's compare view
     */
    Boolean createLinks;

    /**
     * The header to print above the changelog
     */
    String header;

    /**
     * The format for a tag line
     */
    String tag;

    /**
     * The format for the link to the tag history on GitHub
     */
    String tagLink;

    ChangelogFormat apply(ChangelogFormat format) {
        ChangelogFormat original = this;

        return new ChangelogFormat() {{
            branch = Optional.ofNullable(format.branch).
                orElse(original.branch);
            branchLink = Optional.ofNullable(format.branchLink).
                orElse(original.branchLink);
            branchOnlyLink = Optional.ofNullable(format.branchOnlyLink).
                orElse(original.branchOnlyLink);
            commitPrefix = Optional.ofNullable(format.commitPrefix).
                orElse(original.commitPrefix);
            createLinks = Optional.ofNullable(format.createLinks).
                orElse(original.createLinks);
            header = Optional.ofNullable(format.header).
                orElse(original.header);
            tag = Optional.ofNullable(format.tag).
                orElse(original.tag);
            tagLink = Optional.ofNullable(format.tagLink).
                orElse(original.tagLink);
        }};
    }

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
