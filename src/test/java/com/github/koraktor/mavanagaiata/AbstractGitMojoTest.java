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

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.junit.Test;

import org.eclipse.jgit.revwalk.RevCommit;

import junit.framework.TestCase;

public class AbstractGitMojoTest extends TestCase {

    private AbstractGitMojo mojo;

    public void setUp() throws Exception {
        File pom = new File("src/test/resources/test-project/pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pom));
        final MavenProject testProject = new MavenProject(model);
        testProject.setFile(pom.getAbsoluteFile());

        this.mojo = new AbstractGitMojo() {
            {
                this.gitDir = new File("src/test/resources/test-project/_git").getAbsoluteFile();
                this.project = testProject;
            }

            public void execute()
                    throws MojoExecutionException, MojoFailureException {}
        };
    }

    @Test
    public void testDirs() {
        assertNotNull(this.mojo.project);
        assertEquals(new File("src/test/resources/test-project").getAbsoluteFile(), this.mojo.project.getBasedir());
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
        assertEquals("0e7d0435e30d0f726d62ccadd202c9240df56019", head.getName());
    }

}
