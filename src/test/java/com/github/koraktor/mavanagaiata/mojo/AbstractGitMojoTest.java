/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractGitMojoTest extends MojoAbstractTest<AbstractGitMojo> {

    @Before
    @Override
    public void setup() throws Exception {
        this.mojo = new AbstractGitMojo() {
            public void run()
                    throws MojoExecutionException {}
        };

        super.setup();
    }

    @After
    public void tearDown() {
        if (this.mojo != null) {
            this.mojo.cleanup();
        }
    }

    @Test
    public void testErrors() throws Exception {
        this.mojo.baseDir = null;
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(GitRepositoryException.class)));
            assertThat(e.getMessage(), is(equalTo("Neither worktree nor GIT_DIR is set.")));
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
            assertThat(e, is(instanceOf(GitRepositoryException.class)));
            assertThat(e.getMessage(), is(equalTo(this.mojo.baseDir + " is not inside a Git repository. Please specify the GIT_DIR separately.")));
        }

        this.mojo.baseDir = mock(File.class);
        when(this.mojo.baseDir.exists()).thenReturn(false);
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(GitRepositoryException.class)));
            assertThat(e.getMessage(), is(equalTo("The worktree " + this.mojo.baseDir + " does not exist")));
        }

        this.mojo.baseDir = null;
        this.mojo.gitDir  = mock(File.class);
        when(this.mojo.gitDir.exists()).thenReturn(false);
        try {
            this.mojo.initRepository();
            fail("No exception thrown");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(GitRepositoryException.class)));
            assertThat(e.getMessage(), is(equalTo("The GIT_DIR " + this.mojo.gitDir + " does not exist")));
        }

        this.mojo.skipNoGit = true;
        this.mojo.gitDir  = mock(File.class);
        when(this.mojo.gitDir.exists()).thenReturn(false);

        assertThat(this.mojo.init(), is(false));
    }

    @Test
    public void testInitRepository() throws IOException, MojoExecutionException {
        /*this.mojo.initRepository();
        assertThat(this.mojo.project, is(notNullValue()));
        assertThat(this.mojo.repository.getDirectory(),
        is(equalTo(new File(this.getRepository("test-project"), ".git"))));*/
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
