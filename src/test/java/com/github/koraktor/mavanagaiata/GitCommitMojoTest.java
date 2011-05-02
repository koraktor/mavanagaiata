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

public class GitCommitMojoTest extends AbstractMojoTest<GitCommitMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitCommitMojo();

        super.setUp();
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        Properties properties = this.mojo.project.getProperties();

        assertEquals("0e7d0435", properties.get("mavanagaiata.commit.abbrev"));
        assertEquals("0e7d0435e30d0f726d62ccadd202c9240df56019", properties.get("mavanagaiata.commit.id"));
        assertEquals("0e7d0435e30d0f726d62ccadd202c9240df56019", properties.get("mavanagaiata.commit.sha"));
        assertEquals("0e7d0435", properties.get("mvngit.commit.abbrev"));
        assertEquals("0e7d0435e30d0f726d62ccadd202c9240df56019", properties.get("mvngit.commit.id"));
        assertEquals("0e7d0435e30d0f726d62ccadd202c9240df56019", properties.get("mvngit.commit.sha"));
    }

}
