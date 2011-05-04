/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.Test;

public class GitActorsMojoTest extends AbstractMojoTest<GitActorsMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitActorsMojo();

        super.setUp();
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        this.assertProperty("Sebastian Staudt", "author.name");
        this.assertProperty("koraktor@gmail.com", "author.email");
        this.assertProperty("Sebastian Staudt", "committer.name");
        this.assertProperty("koraktor@gmail.com", "committer.email");
    }

}
