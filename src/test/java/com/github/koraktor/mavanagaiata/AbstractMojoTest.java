/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.eclipse.jgit.lib.Repository;

import org.junit.Before;
import org.junit.rules.ExpectedException;

import junit.framework.ComparisonFailure;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public abstract class AbstractMojoTest<T extends AbstractGitMojo> {

    protected static File tempDir;

    protected ExpectedException expectedException = ExpectedException.none();

    protected String headId = "c823991841bda5f99919ae59a4e59b5607f0450b";

    protected T mojo;

    protected Properties projectProperties;

    protected File getRepository(String name) {
        return new File(tempDir, name);
    }

    protected void setupRepositories() throws IOException {
        if (tempDir == null) {
            tempDir = File.createTempFile("mavanagaiata-test", null);
            tempDir.delete();
            tempDir.mkdir();

            FilenameFilter tempFilter = new NotFileFilter(new NameFileFilter("temp"));
            for (File repo : new File("src/test/resources").listFiles(tempFilter)) {
                File tempRepo = new File(tempDir, repo.getName());
                FileUtils.copyDirectory(repo, tempRepo);
                new File(tempRepo, "_git").renameTo(new File(tempRepo, ".git"));
            }

            FileUtils.forceDeleteOnExit(tempDir);
        }
    }

    protected void testError(String errorMessage) {
        try {
            this.mojo.baseDir = this.getRepository("broken-project");
            this.mojo.repository = mock(Repository.class, new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    throw new IOException();
                }
            });
            this.mojo.run();
            fail("No exception thrown.");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(MojoExecutionException.class)));
            assertThat(e.getMessage(), is(equalTo(errorMessage)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }

    @Before
    public void setup() throws Exception {
        this.setupRepositories();

        File pom = new File(this.getRepository("test-project"), "pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        FileReader fileReader = new FileReader(pom);
        Model model = reader.read(fileReader);
        fileReader.close();
        final MavenProject testProject = new MavenProject(model);
        testProject.setFile(pom.getAbsoluteFile());

        this.projectProperties = testProject.getProperties();

        @SuppressWarnings("unchecked")
        Class<T> mojoClass = ((Class<T>)((ParameterizedType) this.getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);
        if(!Modifier.isAbstract(mojoClass.getModifiers())) {
            this.mojo = mojoClass.newInstance();
        }
        this.mojo.baseDir = this.getRepository("test-project");
        this.mojo.project = testProject;
    }

    protected static void assertMatches(String regex, String actual) {
        if(!actual.matches(regex)) {
            throw new ComparisonFailure(null, regex, actual);
        }
    }

    protected void assertProperty(Object value, String key) {
        for(String prefix : this.mojo.propertyPrefixes) {
            assertThat(this.projectProperties.get(prefix + "." + key), is(equalTo(value)));
        }
    }

}
