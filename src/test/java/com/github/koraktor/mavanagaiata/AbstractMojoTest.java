/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import junit.framework.TestCase;

public abstract class AbstractMojoTest<T extends AbstractGitMojo> extends TestCase {

    protected String headId = "c823991841bda5f99919ae59a4e59b5607f0450b";

    protected T mojo;

    protected Properties projectProperties;

    protected void testError(String errorMessage) {
        try {
            this.mojo.gitDir = new File("src/test/resources/broken-project/_git").getAbsoluteFile();
            this.mojo.execute();
            fail("No exception thrown.");
        } catch(Exception e) {
            assertEquals(MojoExecutionException.class, e.getClass());
            assertEquals(errorMessage, e.getMessage());
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }

    public void setUp() throws Exception {
        File pom = new File("src/test/resources/test-project/pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pom));
        final MavenProject testProject = new MavenProject(model);
        testProject.setFile(pom.getAbsoluteFile());

        this.projectProperties = testProject.getProperties();
        this.mojo.gitDir = new File("src/test/resources/test-project/_git").getAbsoluteFile();
        this.mojo.project = testProject;
    }

    protected void assertProperty(String value, String key) {
        for(String prefix : this.mojo.propertyPrefixes) {
            assertEquals(value, this.projectProperties.get(prefix + "." + key));
        }
    }

}
