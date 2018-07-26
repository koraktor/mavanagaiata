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
public class ChangelogDefaultFormat extends ChangelogFormat {

    public ChangelogDefaultFormat() {
        branch = "Commits on branch \"%s\"\n";
        branchLink = "\nSee Git history for changes in the \"%s\" branch since version %s at: %s";
        branchOnlyLink = "\nSee Git history for changes in the \"%s\" branch at: %s";
        commitPrefix = " * ";
        createLinks = true;
        header = "Changelog\n=========\n";
        tag = "\nVersion %s – %s\n";
        tagLink = "\nSee Git history for version %s at: %s";
    }

}
