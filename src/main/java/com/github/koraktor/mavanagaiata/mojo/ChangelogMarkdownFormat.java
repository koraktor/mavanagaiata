/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

/**
 * @author Sebastian Staudt
 */
public class ChangelogMarkdownFormat extends ChangelogDefaultFormat {

    public ChangelogMarkdownFormat() {
        branch = "\n#### Commits on branch `%s`\n";
        branchLink = "\n[Git history for branch `%s` since version %s](%s)";
        branchOnlyLink = "\n[Git history for branch `%s`](%s)";
        escapeHtml = true;
        tag = "\n#### Version %s – %s\n";
        tagLink = "[Git history for branch `%s`](%s)";
    }

}
