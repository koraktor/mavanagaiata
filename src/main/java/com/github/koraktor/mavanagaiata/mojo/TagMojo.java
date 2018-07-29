/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

/**
 * This goal provides the most recent Git tag in the "mavanagaiata.tag" and
 * "mvngit.tag" properties.
 *
 * @author Sebastian Staudt
 * @since 0.1.0
 */
@Mojo(name ="tag",
      defaultPhase = LifecyclePhase.INITIALIZE,
      threadSafe = true)
public class TagMojo extends AbstractGitMojo {

    /**
     * This will first read all tags and walk the commit hierarchy down from
     * HEAD until it finds one of the tags. The name of that tag is written
     * into "mavanagaiata.tag" and "mvngit.tag" respectively.
     *
     * @throws MavanagaiataMojoException if the tags cannot be read
     */
    public void run(GitRepository repository) throws MavanagaiataMojoException {
        try {
            GitTagDescription description = repository.describe();
            String describe = description.toString();
            if (dirtyFlag != null &&
                    repository.isDirty(dirtyIgnoreUntracked)) {
                describe += dirtyFlag;
            }

            addProperty("tag.describe", describe);
            addProperty("tag.name", description.getNextTagName());
        } catch(GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to read Git tag", e);
        }
    }

}
