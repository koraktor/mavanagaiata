/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.Test;

public class GitCommitMojoTest extends AbstractMojoTest<GitCommitMojo> {

    @Test
    public void testError() {
        super.testError("Unable to read Git commit information");
    }

    @Test
    public void testCustomDateFormat() throws MojoExecutionException {
        this.mojo.dateFormat = "dd.MM.yyyy";
        this.mojo.execute();

        this.assertProperty("03.05.2011", "commit.author.date");
        this.assertProperty("06.05.2011", "commit.committer.date");
    }

    @Test
    public void testResult() throws MojoExecutionException {
        this.mojo.execute();

        String commitAbbrev = this.headId.substring(0, 7);
        String email = "koraktor@gmail.com";
        String name  = "Sebastian Staudt";

        this.assertProperty(commitAbbrev, "commit.abbrev");
        this.assertProperty("05/03/2011 09:15 AM +0200", "commit.author.date");
        this.assertProperty(name, "commit.author.name");
        this.assertProperty(email, "commit.author.email");
        this.assertProperty("05/06/2011 12:23 PM +0200", "commit.committer.date");
        this.assertProperty(name, "commit.committer.name");
        this.assertProperty(email, "commit.committer.email");
        this.assertProperty(this.headId, "commit.id");
        this.assertProperty(this.headId, "commit.sha");
    }

}
