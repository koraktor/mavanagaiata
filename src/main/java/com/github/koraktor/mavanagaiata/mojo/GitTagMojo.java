/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

/**
 * This goal provides the most recent Git tag in the "mavanagaiata.tag" and
 * "mvngit.tag" properties.
 *
 * @author Sebastian Staudt
 * @goal tag
 * @phase initialize
 * @requiresProject
 * @since 0.1.0
 */
public class GitTagMojo extends AbstractGitMojo {

    /**
     * This will first read all tags and walk the commit hierarchy down from
     * HEAD until it finds one of the tags. The name of that tag is written
     * into "mavanagaiata.tag" and "mvngit.tag" respectively.
     *
     * @throws MavanagaiataMojoException if the tags cannot be read
     */
    public void run() throws MavanagaiataMojoException {
        try {
            GitTagDescription description = this.repository.describe();
            String describe = description.toString();
            if (this.dirtyFlag != null &&
                    this.repository.isDirty(this.dirtyIgnoreUntracked)) {
                describe += this.dirtyFlag;
            }

            this.addProperty("tag.describe", describe);
            this.addProperty("tag.name", description.getNextTagName());
        } catch(GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to read Git tag", e);
        }
    }

}
