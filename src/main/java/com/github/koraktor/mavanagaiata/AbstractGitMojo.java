/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * This abstract Mojo implements initializing a JGit Repository and provides
 * this Repository instance to subclasses.
 *
 * @author Sebastian Staudt
 * @see Repository
 * @since 0.1.0
 */
public abstract class AbstractGitMojo extends AbstractMojo {

    /**
     * The date format to use for various dates
     *
     * @parameter expression="${mavanagaiata.dateFormat}"
     */
    protected String baseDateFormat = "MM/dd/yyyy hh:mm a Z";

    /**
     * The project base directory
     *
     * @parameter expression="${mavanagaiata.gitDir}"
     *            default-value="${basedir}"
     * @required
     */
    protected File gitDir;

    /**
     * The commit or ref to use as starting point for operations
     *
     * @parameter expression="${mavanagaiata.head}"
     */
    protected String head = "HEAD";

    /**
     * The Maven project
     *
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * The prefixes to prepend to property keys
     *
     * @parameter
     */
    protected String[] propertyPrefixes = { "mavanagaiata", "mvngit" };

    protected Repository repository;

    /**
     * Saves a property with the given name into the project's properties
     *
     * The value will be stored two times – with "mavanagaiata" and "mvngit" as
     * a prefix.
     *
     * @param name The property name
     * @param value The value of the property
     */
    protected void addProperty(String name, String value) {
        Properties properties = this.project.getProperties();

        for(String prefix : this.propertyPrefixes) {
            properties.put(prefix + "." + name, value);
        }
    }

    /**
     * Initializes a JGit Repository object for further reference
     *
     * @see Repository
     * @throws IOException if retrieving information from the Git repository
     *         fails
     */
    protected void initRepository() throws IOException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.readEnvironment();

        if(this.gitDir == null) {
            throw new FileNotFoundException("Git directory is not set");
        } else if(!this.gitDir.exists()) {
            throw new FileNotFoundException(this.gitDir + " does not exist");
        }

        repositoryBuilder.setGitDir(this.gitDir);
        this.repository = repositoryBuilder.build();

        if(!this.repository.getObjectDatabase().exists()) {
            repositoryBuilder.setGitDir(null);
            repositoryBuilder.findGitDir(this.gitDir);

            if(repositoryBuilder.getGitDir() == null) {
                throw new FileNotFoundException(this.gitDir + " is not inside a Git repository");
            }

            repositoryBuilder.setObjectDirectory(null);
            repositoryBuilder.setup();

            this.repository = repositoryBuilder.build();
        }
    }

    /**
     * Returns a commit object for the repository's current HEAD
     *
     * @return The commit object of the repository's current HEAD
     * @see RevCommit
     * @throws IOException if the repository HEAD could not be retrieved
     */
    protected RevCommit getHead() throws IOException {
        if(this.repository == null) {
            this.initRepository();
        }

        RevWalk revWalk = new RevWalk(this.repository);
        ObjectId head = this.repository.resolve(this.head);
        return revWalk.parseCommit(head);
    }

}
