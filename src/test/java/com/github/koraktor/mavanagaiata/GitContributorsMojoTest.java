/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;

import org.junit.Test;

public class GitContributorsMojoTest extends AbstractGitOutputMojoTest<GitContributorsMojo> {

    @Test
    public void testError() {
        super.testError("Unable to read contributors from Git");
    }

    @Test
    public void testCustomization() throws Exception {
        this.mojo.contributorPrefix = "- ";
        this.mojo.header            = "Authors\\n-------\\n";
        this.mojo.showCounts        = false;
        this.mojo.showEmail         = true;
        this.mojo.execute();

        assertEquals("Authors", this.reader.readLine());
        assertEquals("-------", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("- Sebastian Staudt (koraktor@gmail.com)", this.reader.readLine());
        assertEquals("- John Doe (johndoe@example.com)", this.reader.readLine());
        assertFalse(this.reader.ready());
    }

    @Test
    public void testSortDate() throws Exception {
        this.mojo.sort = "date";
        this.mojo.execute();

        this.assertOutput();
    }

    @Test
    public void testSortName() throws Exception {
        this.mojo.sort = "name";
        this.mojo.execute();

        assertEquals("Contributors", this.reader.readLine());
        assertEquals("============", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * John Doe (1)", this.reader.readLine());
        assertEquals(" * Sebastian Staudt (4)", this.reader.readLine());
        assertFalse(this.reader.ready());
    }

    protected void assertOutput() throws IOException {
        assertEquals("Contributors", this.reader.readLine());
        assertEquals("============", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Sebastian Staudt (4)", this.reader.readLine());
        assertEquals(" * John Doe (1)", this.reader.readLine());
        assertFalse(this.reader.ready());
    }

}
