/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.Test;

public class GitTagMojoTest extends AbstractMojoTest<GitTagMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitTagMojo();

        super.setUp();
    }

    @Test
    public void testError() {
        super.testError("Unable to read Git tag");
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        this.mojo.execute();

        String abbrev = this.headId.substring(0, 7);

        this.assertProperty("2.0.0-1-g" + abbrev, "tag.describe");
        this.assertProperty("2.0.0", "tag.name");

        this.mojo.head = "HEAD^";
        this.mojo.execute();

        this.assertProperty("2.0.0", "tag.describe");
        this.assertProperty("2.0.0", "tag.name");

        this.mojo.gitDir = new File("src/test/resources/untagged-project/_git");
        this.mojo.head = "HEAD";
        this.mojo.initRepository();
        this.mojo.execute();

        this.assertProperty("2be4488", "tag.describe");
        this.assertProperty("", "tag.name");
    }

}
