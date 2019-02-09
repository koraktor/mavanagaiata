/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

import org.junit.jupiter.api.BeforeEach;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.jgit.JGitRepository;

import static org.eclipse.jgit.lib.Constants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sebastian Staudt
 */
public abstract class MojoAbstractTest<T extends AbstractGitMojo> {

    protected T mojo;

    private Properties projectProperties;

    protected GitRepository repository;

    protected void testError(String errorMessage) {
        repository = mock(JGitRepository.class, invocationOnMock -> {
            throw new GitRepositoryException("");
        });

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo(errorMessage)));
        assertThat(e.getCause(), is(instanceOf(GitRepositoryException.class)));
    }

    @BeforeEach
    public void setup() throws Exception {
        File baseDir = mock(File.class);
        when(baseDir.exists()).thenReturn(true);

        MavenProject project   = mock(MavenProject.class);
        this.projectProperties = new Properties();
        when(project.getProperties()).thenReturn(this.projectProperties);

        this.repository = mock(GitRepository.class, RETURNS_DEEP_STUBS);

        @SuppressWarnings("unchecked")
        Class<T> mojoClass = ((Class<T>)((ParameterizedType) this.getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);
        if (!Modifier.isAbstract(mojoClass.getModifiers())) {
            this.mojo = mojoClass.getConstructor().newInstance();
        }
        this.mojo.dateFormat            = "MM/dd/yyyy hh:mm a Z";
        this.mojo.baseDir               = baseDir;
        this.mojo.dirtyFlag             = "-dirty";
        this.mojo.dirtyIgnoreUntracked  = false;
        this.mojo.head                  = HEAD;
        this.mojo.project               = project;
    }

    void assertProperty(Object value, String key) {
        for(String prefix : this.mojo.propertyPrefixes) {
            assertThat(this.projectProperties.get(prefix + "." + key), is(equalTo(value)));
        }
    }

}
