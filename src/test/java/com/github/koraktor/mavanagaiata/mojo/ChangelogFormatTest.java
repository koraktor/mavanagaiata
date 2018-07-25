/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class ChangelogFormatTest {

    @Test
    public void testDefaultFormat() {
        ChangelogFormat format = new ChangelogFormat();

        assertThat(format.branch, is(equalTo("Commits on branch \"%s\"\n")));
        assertThat(format.branchLink, is(equalTo("\nSee Git history for changes in the \"%s\" branch since version %s at: %s")));
        assertThat(format.branchOnlyLink, is(equalTo("\nSee Git history for changes in the \"%s\" branch at: %s")));
        assertThat(format.commitPrefix, is(equalTo(" * ")));
        assertThat(format.header, is(equalTo("Changelog\n=========\n")));
        assertThat(format.tag, is(equalTo("\nVersion %s – %s\n")));
        assertThat(format.tagLink, is(equalTo("\nSee Git history for version %s at: %s")));
    }

    @Test
    public void testPrepare() {
        ChangelogFormat format = new ChangelogFormat();
        format.branch = "Test\\nnew line";
        format.branchLink = "Test\\nnew line";
        format.branchOnlyLink = "Test\\nnew line";
        format.commitPrefix = "Test\\nnew line";
        format.header = "Test\\nnew line";
        format.tag = "Test\\nnew line";
        format.tagLink = "Test\\nnew line";

        format.prepare();

        assertThat(format.branch, is(equalTo("Test\nnew line")));
        assertThat(format.branchLink, is(equalTo("Test\nnew line")));
        assertThat(format.branchOnlyLink, is(equalTo("Test\nnew line")));
        assertThat(format.commitPrefix, is(equalTo("Test\nnew line")));
        assertThat(format.header, is(equalTo("Test\nnew line")));
        assertThat(format.tag, is(equalTo("Test\nnew line")));
        assertThat(format.tagLink, is(equalTo("Test\nnew line")));
    }



}
