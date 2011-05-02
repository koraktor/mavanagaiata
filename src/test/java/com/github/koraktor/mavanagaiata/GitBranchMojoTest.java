/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.Test;

public class GitBranchMojoTest extends AbstractMojoTest<GitBranchMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitBranchMojo();

        super.setUp();
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        Properties properties = this.mojo.project.getProperties();

        assertEquals("master", properties.get("mavanagaiata.branch"));
        assertEquals("master", properties.get("mvngit.branch"));
    }

}
