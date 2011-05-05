/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class GitChangelogMojoTest extends AbstractMojoTest<GitChangelogMojo> {

    private File tempFile;

    @Override
    public void setUp() throws Exception {
        this.mojo = new GitChangelogMojo();
        this.tempFile = File.createTempFile("changelog", null);

        this.mojo.outputFile = this.tempFile;

        super.setUp();
    }

    @Test
    public void testResult() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(this.tempFile));

        assertEquals("Changelog", reader.readLine());
        assertEquals("=========", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals(" * Snapshot for version 3.0.0", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("Version 2.0.0 - 05/03/2011 07:18 AM", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals(" * Version bump to 2.0.0", reader.readLine());
        assertEquals(" * Snapshot for version 2.0.0", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("Version 1.0.0 - 05/03/2011 07:18 AM", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals(" * Initial commit", reader.readLine());
        assertFalse(reader.ready());
    }

    @Test
    public void testSkipTagged() throws Exception {
        this.mojo.skipTagged = true;
        this.mojo.execute();
        BufferedReader reader = new BufferedReader(new FileReader(this.tempFile));

        assertEquals("Changelog", reader.readLine());
        assertEquals("=========", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals(" * Snapshot for version 3.0.0", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("Version 2.0.0 - 05/03/2011 07:18 AM", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals(" * Snapshot for version 2.0.0", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("Version 1.0.0 - 05/03/2011 07:18 AM", reader.readLine());
        assertEquals("", reader.readLine());
        assertFalse(reader.ready());
    }

}
