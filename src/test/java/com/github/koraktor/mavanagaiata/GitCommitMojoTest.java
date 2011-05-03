/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.Date;
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
        Date commitDate = new Date(1304406915000L);
        String commitAbbrev = this.headId.substring(0, 8);

        assertEquals(commitAbbrev, properties.get("mavanagaiata.commit.abbrev"));
        assertEquals(commitDate, properties.get("mavanagaiata.commit.date"));
        assertEquals(this.headId, properties.get("mavanagaiata.commit.id"));
        assertEquals(this.headId, properties.get("mavanagaiata.commit.sha"));
        assertEquals(commitAbbrev, properties.get("mvngit.commit.abbrev"));
        assertEquals(commitDate, properties.get("mvngit.commit.date"));
        assertEquals(this.headId, properties.get("mvngit.commit.id"));
        assertEquals(this.headId, properties.get("mvngit.commit.sha"));
    }

}
