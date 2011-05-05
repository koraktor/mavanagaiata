/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.Test;

public class GitChangelogMojoTest extends AbstractMojoTest<GitChangelogMojo> {

    private BufferedReader reader;

    @Override
    public void setUp() throws Exception {
        File tempFile = File.createTempFile("changelog", null);
        this.mojo = new GitChangelogMojo();
        this.mojo.outputFile = tempFile;
        this.reader = new BufferedReader(new FileReader(tempFile));

        super.setUp();
    }

    @Test
    public void testError() {
        super.testError("Unable to generate changelog from Git");
    }

    @Test
    public void testCustomization() throws Exception {
        this.mojo.commitPrefix = "- ";
        this.mojo.dateFormat   = "dd.MM.yyyy";
        this.mojo.header       = "History\\n-------\\n";
        this.mojo.tagPrefix    = "\nTag ";
        this.mojo.execute();

        assertEquals("History", this.reader.readLine());
        assertEquals("-------", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("- Snapshot for version 3.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Tag 2.0.0 - 03.05.2011", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("- Version bump to 2.0.0", this.reader.readLine());
        assertEquals("- Snapshot for version 2.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Tag 1.0.0 - 03.05.2011", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("- Initial commit", reader.readLine());
        assertFalse(this.reader.ready());
    }

    @Test
    public void testResult() throws Exception {
        this.mojo.execute();

        assertEquals("Changelog", this.reader.readLine());
        assertEquals("=========", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Snapshot for version 3.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Version 2.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Version bump to 2.0.0", this.reader.readLine());
        assertEquals(" * Snapshot for version 2.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Version 1.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Initial commit", reader.readLine());
        assertFalse(this.reader.ready());
    }

    @Test
    public void testSkipTagged() throws Exception {
        this.mojo.skipTagged = true;
        this.mojo.execute();

        assertEquals("Changelog", this.reader.readLine());
        assertEquals("=========", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Snapshot for version 3.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Version 2.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Snapshot for version 2.0.0", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals("Version 1.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertFalse(this.reader.ready());
    }

    @Test
    public void testStdOut() throws Exception {
        try {
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(oStream);
            System.setOut(stream);

            this.mojo.outputFile = null;
            this.mojo.execute();

            byte[] output = oStream.toByteArray();
            this.reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output)));

            assertEquals("Changelog", this.reader.readLine());
            assertEquals("=========", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals(" * Snapshot for version 3.0.0", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals("Version 2.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals(" * Version bump to 2.0.0", this.reader.readLine());
            assertEquals(" * Snapshot for version 2.0.0", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals("Version 1.0.0 - 05/03/2011 07:18 AM", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals(" * Initial commit", reader.readLine());
            assertFalse(this.reader.ready());
        } finally {
            System.setOut(null);
        }
    }

}
