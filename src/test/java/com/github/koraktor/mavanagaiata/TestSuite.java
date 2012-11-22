/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AbstractGitMojoTest.class,
        GitBranchMojoTest.class,
        GitChangelogMojoTest.class,
        GitCommitMojoTest.class,
        GitContributorsMojoTest.class,
        GitTagMojoTest.class
})
public class TestSuite {

    @BeforeClass
    public static void setup() {
        for (File project : new File("src/test/resources").listFiles()) {
            new File(project, "_git").renameTo(new File(project, ".git"));
        }
    }

    @AfterClass
    public static void teardown() {
        for (File project : new File("src/test/resources").listFiles()) {
            new File(project, ".git").renameTo(new File(project, "_git"));
        }
    }

}
