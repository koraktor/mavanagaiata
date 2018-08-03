/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.PrintStream;
import java.util.GregorianCalendar;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangelogFormatTest {

    @Test
    public void testPrepare() {
        ChangelogFormat format = new ChangelogFormat();
        format.branch = "Test\\nnew line";
        format.branchLink = "Test\\nnew line";
        format.branchOnlyLink = "Test\\nnew line";
        format.dateFormat = "MM/dd/yyyy";
        format.commitPrefix = "Test\\nnew line";
        format.header = "A\\nheader";
        format.separator = "Test\\nnew line";
        format.tag = "Test\\nnew line";
        format.tagLink = "Test\\nnew line";

        format.prepare();

        assertThat(format.branch, is(equalTo("Test\nnew line")));
        assertThat(format.branchLink, is(equalTo("Test\nnew line")));
        assertThat(format.branchOnlyLink, is(equalTo("Test\nnew line")));
        assertThat(format.dateFormatter.format(new GregorianCalendar(2011, 3, 29).getTime()), is(equalTo("04/29/2011")));
        assertThat(format.commitPrefix, is(equalTo("Test\nnew line")));
        assertThat(format.header, is(equalTo("A\nheader")));
        assertThat(format.separator, is(equalTo("Test\nnew line")));
        assertThat(format.tag, is(equalTo("Test\nnew line")));
        assertThat(format.tagLink, is(equalTo("Test\nnew line")));
    }
}
