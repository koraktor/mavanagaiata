/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.Date;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.Test;

public class GitCommitMojoTest extends AbstractMojoTest<GitCommitMojo> {

    public void setUp() throws Exception {
        this.mojo = new GitCommitMojo();

        super.setUp();
    }

    @Test
    public void testError() {
        super.testError("Unable to read Git commit information");
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        this.mojo.execute();

        String commitDate = new Date(1304406915000L).toString();
        String commitAbbrev = this.headId.substring(0, 7);
        String email = "koraktor@gmail.com";
        String name  = "Sebastian Staudt";

        this.assertProperty(commitAbbrev, "commit.abbrev");
        this.assertProperty(name, "commit.author.name");
        this.assertProperty(email, "commit.author.email");
        this.assertProperty(name, "commit.committer.name");
        this.assertProperty(email, "commit.committer.email");
        this.assertProperty(commitDate, "commit.date");
        this.assertProperty(this.headId, "commit.id");
        this.assertProperty(this.headId, "commit.sha");
    }

}
