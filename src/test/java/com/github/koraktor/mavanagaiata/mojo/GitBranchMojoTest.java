/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
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
        when(repository.getBranch()).thenReturn("master");

        mojo.run(repository);

        assertProperty("master", "branch");
    }

}
