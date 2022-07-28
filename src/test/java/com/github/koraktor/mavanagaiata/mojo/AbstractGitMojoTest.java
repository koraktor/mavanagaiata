/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.mockito.InOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;

import static org.eclipse.jgit.lib.Constants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsSame.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sebastian Staudt
 */
@DisplayName("AbstractGitMojo")
class AbstractGitMojoTest extends MojoAbstractTest<AbstractGitMojo> {

    @BeforeEach
    @Override
    public void setup() throws Exception {
        mojo = spy(new AbstractGitMojo() {
            public void run(GitRepository repository)
                throws MavanagaiataMojoException {}
        });

        super.setup();
    }

    @DisplayName("should handle different errors while accessing the repository")
    @Test
    void testErrors() {
        this.mojo.baseDir = null;

        GitRepositoryException e =assertThrows(GitRepositoryException.class,
            mojo::initRepository);
        assertThat(e.getMessage(), is(equalTo("Neither worktree nor GIT_DIR is set.")));

        String home = System.getenv().get("HOME");
        if (home == null) {
            home = System.getenv().get("HOMEDRIVE") + System.getenv("HOMEPATH");
        }
        this.mojo.baseDir = new File(home).getAbsoluteFile();

        e = assertThrows(GitRepositoryException.class,
            mojo::initRepository);
        assertThat(e.getMessage(), is(equalTo(mojo.baseDir + " is not inside a Git repository. Please specify the GIT_DIR separately.")));

        this.mojo.baseDir = mock(File.class);
        when(this.mojo.baseDir.exists()).thenReturn(false);

        e = assertThrows(GitRepositoryException.class,
            mojo::initRepository);
        assertThat(e.getMessage(), is(equalTo("The worktree " + mojo.baseDir + " does not exist")));

        this.mojo.baseDir = null;
        this.mojo.gitDir  = mock(File.class);
        when(this.mojo.gitDir.exists()).thenReturn(false);

        e = assertThrows(GitRepositoryException.class,
            mojo::initRepository);
        assertThat(e.getMessage(), is(equalTo("The GIT_DIR " + mojo.gitDir + " does not exist")));
    }

    @DisplayName("should provide a basic workflow for mojos")
    @Test
    void testExecute() throws Exception {
        doReturn(repository).when(mojo).initRepository();

        this.mojo.execute();

        InOrder inOrder = inOrder(this.mojo);
        inOrder.verify(this.mojo).init();
        inOrder.verify(this.mojo).prepareParameters();
        inOrder.verify(mojo).run(repository);
    }

    @DisplayName("should handle errors thrown inside mojos")
    @Test
    void testExecuteFail() throws Exception {
        MavanagaiataMojoException exception = MavanagaiataMojoException.create("", null);
        doThrow(exception).when(mojo).run(repository);
        doReturn(repository).when(mojo).initRepository();

        MojoExecutionException e = assertThrows(MojoExecutionException.class,
            mojo::execute);
        assertThat(e.getCause(), is(sameInstance(exception)));
        assertThat(e.getMessage(), is(equalTo(exception.getMessage())));
    }

    @DisplayName("should handle errors gracefully thrown inside mojos")
    @Test
    void testExecuteFailGracefully() throws Exception {
        this.mojo.failGracefully = true;

        MavanagaiataMojoException exception = MavanagaiataMojoException.create("", null);
        doThrow(exception).when(mojo).run(repository);
        doReturn(repository).when(this.mojo).initRepository();

        MojoFailureException e = assertThrows(MojoFailureException.class,
            mojo::execute);
        assertThat(e.getCause(), is(sameInstance(exception)));
        assertThat(e.getMessage(), is(equalTo(exception.getMessage())));
    }

    @DisplayName("should not run when no repository has been initialized")
    @Test
    void testExecuteInitFail() throws Exception {
        doReturn(null).when(mojo).init();

        this.mojo.execute();

        verify(mojo, never()).run(repository);
    }

    @DisplayName("should be able to be skipped")
    @Test
    void testExecuteSkip() throws Exception {
        this.mojo.skip = true;
        this.mojo.execute();

        verify(this.mojo, never()).init();
        verify(mojo, never()).run(repository);
    }

    @DisplayName("should initialize the repository")
    @Test
    void testInit() throws Exception {
        doReturn(repository).when(this.mojo).initRepository();

        assertThat(mojo.init(), is(notNullValue()));
    }

    @DisplayName("should handle errors while initializing")
    @Test
    void testInitError() throws Exception {
        GitRepositoryException exception = new GitRepositoryException("");
        doThrow(exception).when(this.mojo).initRepository();

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            mojo::init);
        assertThat(e.getCause(), is(sameInstance(exception)));
        assertThat(e.getMessage(), is(equalTo("Unable to initialize Git repository")));
    }

    @DisplayName("should ignore errors when skipNoGit is set")
    @Test
    void testInitErrorSkipNoGit() throws Exception {
        this.mojo.skipNoGit = true;

        doThrow(new GitRepositoryException("")).when(this.mojo).initRepository();

        assertThat(mojo.init(), is(nullValue()));
    }

    @DisplayName("should ignore errors when skipNoGit is set")
    @Test
    void testInitRepository() throws Exception {
        File baseDir = Files.createTempDirectory("mavanagaiata-tests-baseDir").toFile();
        FileUtils.forceDeleteOnExit(baseDir);

        File gitDir = File.createTempFile("mavanagaiata-tests-gitDir", null);
        gitDir.delete();
        new File(gitDir, "objects").mkdirs();
        FileUtils.forceDeleteOnExit(gitDir);

        this.mojo.baseDir = baseDir;
        this.mojo.gitDir = gitDir;
        this.mojo.head = HEAD;

        GitRepository repository = mojo.initRepository();

        assertThat(repository.getHeadRef(), is(equalTo(HEAD)));
        assertThat(repository.getWorkTree(), is(baseDir));
        assertThat(repository.isChecked(), is(true));
    }

    @DisplayName("should ignore non-existant repositories when skipNoGit is set")
    @Test
    void testSkipNoGit() throws Exception{
        this.mojo.skipNoGit = true;
        this.mojo.gitDir  = mock(File.class);
        when(this.mojo.gitDir.exists()).thenReturn(false);

        assertThat(mojo.init(), is(nullValue()));
    }

    @DisplayName("should provide a generic way to add prefixed properties")
    @Test
    void testAddProperty() {
        Properties properties = this.mojo.project.getProperties();

        this.mojo.addProperty("name", "value");

        this.assertProperty("value", "name");

        this.mojo.propertyPrefixes = new String[] { "prefix" };
        this.mojo.addProperty("prefixed", "value");

        this.assertProperty("value", "prefixed");
        assertThat(properties.get("mavanagaiata.prefixed"), is(nullValue()));
        assertThat(properties.get("mvngit.prefixed"), is(nullValue()));
    }

}
