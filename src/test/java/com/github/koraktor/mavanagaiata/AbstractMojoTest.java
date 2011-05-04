/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import junit.framework.TestCase;

public abstract class AbstractMojoTest<T extends AbstractGitMojo> extends TestCase {

    protected String headId = "2be448893a536c2a6b221056ad61536c47c8354c";

    protected T mojo;

    public void setUp() throws Exception {
        File pom = new File("src/test/resources/test-project/pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pom));
        final MavenProject testProject = new MavenProject(model);
        testProject.setFile(pom.getAbsoluteFile());

        this.mojo.gitDir = new File("src/test/resources/test-project/_git").getAbsoluteFile();
        this.mojo.project = testProject;
        this.mojo.execute();
    }

}
