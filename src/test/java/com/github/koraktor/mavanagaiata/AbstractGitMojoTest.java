/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.revwalk.RevCommit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AbstractGitMojoTest extends AbstractMojoTest<AbstractGitMojo> {

    @Before
    public void setup() throws Exception {
        this.mojo = new AbstractGitMojo() {
            public void run()
                    throws MojoExecutionException {}
        };

        super.setup();
        this.mojo.init();
    }

    @After
    public void tearDown() {
        if (this.mojo != null) {
            this.mojo.cleanup();
        }
    }

    @Test
    public void testDirs() {
        assertThat(this.mojo.project, is(notNullValue()));
        assertThat(this.mojo.project.getBasedir(), is(equalTo(this.getRepository("test-project"))));
    }

    @Test
    public void testDirty() throws IOException, MojoExecutionException {
        assertThat(this.mojo.isDirty(), is(false));

        this.mojo.cleanup();
        this.mojo.baseDir = this.getRepository("dirty-project");
        this.mojo.init();

        assertThat(this.mojo.isDirty(), is(true));
    }

    @Test
    public void testErrors() {
        this.mojo.baseDir = null;
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(MojoExecutionException.class)));
            assertThat(e.getMessage(), is(equalTo("Neither baseDir nor gitDir is set.")));
        }

        String home = System.getenv().get("HOME");
        if (home == null) {
            home = System.getenv().get("HOMEDRIVE") + System.getenv("HOMEPATH");
        }
        this.mojo.baseDir = new File(home).getAbsoluteFile();
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), is(equalTo(this.mojo.baseDir + " is not a Git repository")));
        }

        this.mojo.baseDir = this.getRepository("non-existant-project");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), is(equalTo("The baseDir " + this.mojo.baseDir + " does not exist")));
        }

        this.mojo.baseDir = null;
        this.mojo.gitDir  = new File(this.getRepository("non-existant-project"), ".git");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(FileNotFoundException.class)));
            assertThat(e.getMessage(), is(equalTo("The gitDir " + this.mojo.gitDir + " does not exist")));
        }

        this.mojo.baseDir = null;
        this.mojo.gitDir  = new File(this.getRepository("broken-project"), ".git");
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
            assertThat(e.getMessage(), is(equalTo("Unknown repository format \"42\"; expected \"0\".")));
        }
    }

    @Test
    public void testInitRepository() throws IOException, MojoExecutionException {
        this.mojo.initRepository();
        assertThat(this.mojo.project, is(notNullValue()));
        assertThat(this.mojo.repository.getDirectory(),
           is(equalTo(new File(this.getRepository("test-project"), ".git"))));
    }

    @Test
    public void testGetHead() throws IOException, MojoExecutionException {
        RevCommit head = this.mojo.getHead();
        assertThat(head.getName(), is(equalTo(this.headId)));
    }

    @Test
    public void testRelativeHead() throws IOException, MojoExecutionException {
        String head;

        this.mojo.head = "HEAD^";
        head = this.mojo.getHead().getName();
        assertThat(head, is(equalTo("f391f31093fd200534a4fb2e517af89efbdc5fe5")));

        this.mojo.head = "HEAD~3";
        head = this.mojo.getHead().getName();
        assertThat(head, is(equalTo("d50fdcd2858ac9531d6dd87c1de3b623fa243204")));

        this.mojo.head = "HEAD^~2^";
        head = this.mojo.getHead().getName();
        assertThat(head, is(equalTo("0e7d0435e30d0f726d62ccadd202c9240df56019")));
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
