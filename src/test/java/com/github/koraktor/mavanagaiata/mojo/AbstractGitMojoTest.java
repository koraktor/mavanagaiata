/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import org.codehaus.plexus.util.FileUtils;
import org.mockito.InOrder;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractGitMojoTest extends MojoAbstractTest<AbstractGitMojo> {

    @Before
    @Override
    public void setup() throws Exception {
        mojo = spy(new AbstractGitMojo() {
            public void run(GitRepository repository)
                    throws MavanagaiataMojoException {}
        });

        super.setup();
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
    }

    @Test
    public void testExecute() throws Exception {
        doReturn(repository).when(mojo).initRepository();

        this.mojo.execute();

        InOrder inOrder = inOrder(this.mojo);
        inOrder.verify(this.mojo).init();
        inOrder.verify(this.mojo).prepareParameters();
        inOrder.verify(mojo).run(repository);
    }

    @Test
    public void testExecuteFail() throws Exception {
        MavanagaiataMojoException exception = MavanagaiataMojoException.create("", null);
        doThrow(exception).when(mojo).run(repository);
        doReturn(repository).when(mojo).initRepository();

        try {
            this.mojo.execute();
            fail("No exception thrown.");
        } catch (MojoExecutionException e) {
            assertThat(e.getCause(), is(instanceOf(MavanagaiataMojoException.class)));
            assertThat((MavanagaiataMojoException) e.getCause(), is(sameInstance(exception)));
            assertThat(e.getMessage(), is(equalTo(exception.getMessage())));
        }
    }

    @Test
    public void testExecuteFailGracefully() throws Exception {
        this.mojo.failGracefully = true;

        MavanagaiataMojoException exception = MavanagaiataMojoException.create("", null);
        doThrow(exception).when(mojo).run(repository);
        doReturn(repository).when(this.mojo).initRepository();

        try {
            this.mojo.execute();
            fail("No exception thrown.");
        } catch (MojoFailureException e) {
            assertThat(e.getCause(), is(instanceOf(MavanagaiataMojoException.class)));
            assertThat((MavanagaiataMojoException) e.getCause(), is(sameInstance(exception)));
            assertThat(e.getMessage(), is(equalTo(exception.getMessage())));
        }
    }

    @Test
    public void testExecuteInitFail() throws Exception {
        doReturn(null).when(mojo).init();

        this.mojo.execute();

        verify(mojo, never()).run(repository);
    }

    @Test
    public void testExecuteSkip() throws Exception {
        this.mojo.skip = true;
        this.mojo.execute();

        verify(this.mojo, never()).init();
        verify(mojo, never()).run(repository);
    }

    @Test
    public void testInit() throws Exception {
        doReturn(repository).when(this.mojo).initRepository();

        assertThat(mojo.init(), is(notNullValue()));
    }

    @Test
    public void testInitError() throws Exception {
        GitRepositoryException exception = new GitRepositoryException("");
        doThrow(exception).when(this.mojo).initRepository();

        try {
            this.mojo.init();
            fail("No exception thrown.");
        } catch (MavanagaiataMojoException e) {
            assertThat(e.getCause(), is(instanceOf(GitRepositoryException.class)));
            assertThat((GitRepositoryException) e.getCause(), is(sameInstance(exception)));
            assertThat(e.getMessage(), is(equalTo("Unable to initialize Git repository")));
        }
    }

    @Test
    public void testInitErrorSkipNoGit() throws Exception {
        this.mojo.skipNoGit = true;

        doThrow(new GitRepositoryException("")).when(this.mojo).initRepository();

        assertThat(mojo.init(), is(nullValue()));
    }

    @Test
    public void testInitRepository() throws Exception {
        File baseDir = File.createTempFile("mavanagaiata-tests-baseDir", null);
        baseDir.delete();
        baseDir.mkdirs();
        FileUtils.forceDeleteOnExit(baseDir);

        File gitDir = File.createTempFile("mavanagaiata-tests-gitDir", null);
        gitDir.delete();
        new File(gitDir, "objects").mkdirs();
        FileUtils.forceDeleteOnExit(gitDir);

        this.mojo.baseDir = baseDir;
        this.mojo.gitDir = gitDir;
        this.mojo.head = "HEAD";

        GitRepository repository = mojo.initRepository();

        assertThat(repository.getHeadRef(), is(equalTo("HEAD")));
        assertThat(repository.getWorkTree(), is(baseDir));
        assertThat(repository.isChecked(), is(true));
    }

    @Test
    public void testSkip() throws Exception {
        mojo.skip = true;

        mojo.execute();

        verify(mojo, never()).init();
    }

    @Test
    public void testSkipNoGit() throws Exception{
        this.mojo.skipNoGit = true;
        this.mojo.gitDir  = mock(File.class);
        when(this.mojo.gitDir.exists()).thenReturn(false);

        assertThat(mojo.init(), is(nullValue()));
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
