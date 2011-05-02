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

public class GitActorsMojoTest extends AbstractMojoTest<GitActorsMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitActorsMojo();

        super.setUp();
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        Properties properties = this.mojo.project.getProperties();

        assertEquals("Sebastian Staudt", properties.get("mavanagaiata.author.name"));
        assertEquals("koraktor@gmail.com", properties.get("mavanagaiata.author.email"));
        assertEquals("Sebastian Staudt", properties.get("mavanagaiata.committer.name"));
        assertEquals("koraktor@gmail.com", properties.get("mavanagaiata.committer.email"));
        assertEquals("Sebastian Staudt", properties.get("mvngit.author.name"));
        assertEquals("koraktor@gmail.com", properties.get("mvngit.author.email"));
        assertEquals("Sebastian Staudt", properties.get("mvngit.committer.name"));
        assertEquals("koraktor@gmail.com", properties.get("mvngit.committer.email"));
    }

}
