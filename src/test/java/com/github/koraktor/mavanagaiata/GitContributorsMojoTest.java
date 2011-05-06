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

public class GitContributorsMojoTest extends AbstractMojoTest<GitContributorsMojo> {

    private BufferedReader reader;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        File tempFile = File.createTempFile("contributors", null);
        this.mojo.outputFile = tempFile;
        this.reader = new BufferedReader(new FileReader(tempFile));
    }

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
    public void testResult() throws Exception {
        this.mojo.execute();

        assertEquals("Contributors", this.reader.readLine());
        assertEquals("============", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Sebastian Staudt (4)", this.reader.readLine());
        assertEquals(" * John Doe (1)", this.reader.readLine());
        assertFalse(this.reader.ready());
    }

    @Test
    public void testSortDate() throws Exception {
        this.mojo.sort = "date";
        this.mojo.execute();

        assertEquals("Contributors", this.reader.readLine());
        assertEquals("============", this.reader.readLine());
        assertEquals("", this.reader.readLine());
        assertEquals(" * Sebastian Staudt (4)", this.reader.readLine());
        assertEquals(" * John Doe (1)", this.reader.readLine());
        assertFalse(this.reader.ready());
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

            assertEquals("Contributors", this.reader.readLine());
            assertEquals("============", this.reader.readLine());
            assertEquals("", this.reader.readLine());
            assertEquals(" * Sebastian Staudt (4)", this.reader.readLine());
            assertEquals(" * John Doe (1)", this.reader.readLine());
            assertFalse(this.reader.ready());
        } finally {
            System.setOut(null);
        }
    }

}
