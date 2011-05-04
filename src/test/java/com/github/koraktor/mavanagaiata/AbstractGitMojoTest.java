/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.IOException;

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
    public void testError() {
        this.mojo.gitDir = new File("src/test/resources/broken-project/_git");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertEquals(MojoExecutionException.class, e.getClass());
            assertEquals("Unable to read Git repository", e.getMessage());
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
        this.mojo.addProperty("name", "value");

        assertEquals("value", this.mojo.project.getProperties().get("mavanagaiata.name"));
        assertEquals("value", this.mojo.project.getProperties().get("mvngit.name"));
    }

}
