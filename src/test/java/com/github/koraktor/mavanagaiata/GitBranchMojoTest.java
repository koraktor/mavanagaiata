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

public class GitBranchMojoTest extends AbstractMojoTest<GitBranchMojo> {

    @Test
    public void testError() {
        super.testError("Unable to read Git branch");
    }

    @Test
    public void testResult() throws IOException, MojoExecutionException {
        this.mojo.execute();

        this.assertProperty("master", "branch");
    }

}
