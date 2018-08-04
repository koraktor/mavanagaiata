/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;

class BranchMojoTest extends MojoAbstractTest<BranchMojo> {

    @Test
    void testError() {
        super.testError("Unable to read Git branch");
    }

    @Test
    void testResult() throws Exception {
        when(repository.getBranch()).thenReturn("master");

        mojo.run(repository);

        assertProperty("master", "branch");
    }

}
