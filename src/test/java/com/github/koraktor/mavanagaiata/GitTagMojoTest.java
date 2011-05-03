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

public class GitTagMojoTest extends AbstractMojoTest<GitTagMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitTagMojo();

        super.setUp();
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        Properties properties = this.mojo.project.getProperties();

        assertEquals("2.0.0", properties.get("mavanagaiata.tag"));
        assertEquals("2.0.0", properties.get("mvngit.tag"));
    }

}
