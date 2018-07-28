/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Optional;

import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitTag;

import static com.github.koraktor.mavanagaiata.mojo.AbstractGitOutputMojo.unescapeFormatNewlines;

/**
 * Base class for formatting changelog output
 * <p>
 * Individual properties can be overridden in the configuration of the
 * {@code changelog} mojo.
 *
 * @author Sebastian Staudt
 * @see ChangelogMojo
 * @see ChangelogDefaultFormat
 * @see ChangelogMarkdownFormat
 */
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

    String baseUrl;

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

    SimpleDateFormat dateFormatter;

    String dateFormat;

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

    private PrintStream printStream;

    /**
     * THe separator to print between different sections of the changelog
     */
    String separator;

    /**
     * The format for a tag line
     */
    String tag;

    /**
     * The format for the link to the tag history on GitHub
     */
    String tagLink;

    /**
     * Create a new format instance using this instance as base and override
     * with (non-{@code null}) properties of the given format
     *
     * @param format Format to apply settings from
     * @return A new format with applied settings
     */
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
            separator = Optional.ofNullable(format.separator).
                orElse(original.separator);
            tag = Optional.ofNullable(format.tag).
                orElse(original.tag);
            tagLink = Optional.ofNullable(format.tagLink).
                orElse(original.tagLink);
        }};
    }

    /**
     * Enable creation of links using the given base URL
     *
     * @param baseUrl The base URL to link to
     */
    void enableCreateLinks(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Prepare the format strings for use
     */
    void prepare(PrintStream printStream) {
        branch = unescapeFormatNewlines(branch);
        branchLink = unescapeFormatNewlines(branchLink);
        branchOnlyLink = unescapeFormatNewlines(branchOnlyLink);
        commitPrefix = unescapeFormatNewlines(commitPrefix);
        header = unescapeFormatNewlines(header);
        separator = unescapeFormatNewlines(separator);
        tag = unescapeFormatNewlines(tag);
        tagLink = unescapeFormatNewlines(tagLink);

        dateFormatter = new SimpleDateFormat(dateFormat);
        this.printStream = printStream;
    }

    /**
     * Print a section header for a branch
     *
     * @param branchName The name of the branch
     */
    void printBranch(String branchName) {
        printStream.println(separator + String.format(branch, branchName) + separator);
    }

    /**
     * Print a single line for a commit
     *
     * @param currentCommit The commit to print
     */
    void printCommit(GitCommit currentCommit) {
        printStream.println(commitPrefix + currentCommit.getMessageSubject());
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
     * @param currentRef The current tag or branch in the changelog
     * @param lastRef The last tag or branch in the changelog
     * @param isBranch Whether the current ref is a branch
     */
    void printCompareLink(String currentRef, String lastRef, boolean isBranch) {
        if (baseUrl == null) {
            return;
        }

        String url = baseUrl;
        if (lastRef == null) {
            url += String.format("/commits/%s", currentRef);
        } else {
            url += String.format("/compare/%s...%s", currentRef, lastRef);
        }

        String linkText;
        if (isBranch) {
            if (lastRef == null) {
                linkText = String.format(branchOnlyLink, currentRef, url);
            } else {
                linkText = String.format(branchLink, lastRef, currentRef, url);
            }
        } else {
            String tagName = (lastRef == null) ? currentRef : lastRef;
            linkText = String.format(tagLink, tagName, url);
        }

        printStream.println(linkText + separator);
    }

    /**
     * Print a header for the changelog
     */
    void printHeader() {
        printStream.println(header);
    }

    /**
     * Print a separator between sections
     */
    void printSeparator() {
        printStream.print(separator);
    }

    /**
     * Print a section header for a tag
     *
     * @param currentTag The tag
     */
    void printTag(GitTag currentTag) {
        dateFormatter.setTimeZone(currentTag.getTimeZone());
        String date = dateFormatter.format(currentTag.getDate());

        printStream.println(String.format(tag, currentTag.getName(), date) + separator);
    }

}
