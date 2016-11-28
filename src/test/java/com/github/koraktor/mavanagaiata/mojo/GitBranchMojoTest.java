/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.junit.Test;

import static org.mockito.Mockito.when;

public class GitBranchMojoTest extends MojoAbstractTest<GitBranchMojo> {

    @Test
    public void testError() {
        super.testError("Unable to read Git branch");
    }

    @Test
    public void testResult() throws Exception {
        when(this.repository.getBranch()).thenReturn("master");

        this.mojo.run();

        this.assertProperty("master", "branch");
    }

}
