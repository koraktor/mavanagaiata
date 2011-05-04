/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.eclipse.jgit.revwalk.RevCommit;

import org.junit.Test;

public class AbstractGitMojoTest extends AbstractMojoTest<AbstractGitMojo> {

    public void setUp() throws Exception {
        this.mojo = new AbstractGitMojo() {
            public void execute()
                    throws MojoExecutionException, MojoFailureException {}
        };

        super.setUp();
    }

    @Test
    public void testDirs() {
        assertNotNull(this.mojo.project);
        assertEquals(new File("src/test/resources/test-project").getAbsoluteFile(), this.mojo.project.getBasedir());
    }

    @Test
    public void testErrors() {
        this.mojo.gitDir = null;
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertEquals(MojoExecutionException.class, e.getClass());
            assertEquals("Git directory is not set", e.getMessage());
        }

        this.mojo.gitDir = new File("src/test/resources/non-existant-project/_git");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertEquals(MojoExecutionException.class, e.getClass());
            assertEquals("Git directory does not exist", e.getMessage());
            assertEquals(FileNotFoundException.class, e.getCause().getClass());
        }

        this.mojo.gitDir = new File("src/test/resources/broken-project/_git");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertEquals(MojoExecutionException.class, e.getClass());
            assertEquals("Unable to read Git repository", e.getMessage());
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testInitRepository() throws MojoExecutionException {
        this.mojo.initRepository();
        assertNotNull(this.mojo.repository);
        assertEquals(new File("src/test/resources/test-project/_git").getAbsolutePath(),
            this.mojo.repository.getDirectory().getAbsolutePath());
    }

    @Test
    public void testGetHead() throws IOException, MojoExecutionException {
        RevCommit head = this.mojo.getHead();
        assertEquals(this.headId, head.getName());

        this.mojo.initRepository();

        head = this.mojo.getHead();
        assertEquals(this.headId, head.getName());
    }

    @Test
    public void testAddProperty() {
        Properties properties = this.mojo.project.getProperties();

        this.mojo.addProperty("name", "value");

        this.assertProperty("value", "name");

        this.mojo.propertyPrefixes = new String[] { "prefix" };
        this.mojo.addProperty("prefixed", "value");

        this.assertProperty("value", "prefixed");
        assertNull(properties.get("mavanagaiata.prefixed"));
        assertNull(properties.get("mvngit.prefixed"));
    }

}
