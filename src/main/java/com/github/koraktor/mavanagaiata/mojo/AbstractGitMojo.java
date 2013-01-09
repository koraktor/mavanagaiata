/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.jgit.JGitRepository;

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
     * @parameter property="mavanagaiata.dateFormat"
     */
    protected String baseDateFormat = "MM/dd/yyyy hh:mm a Z";

    /**
     * The project's base directory
     *
     * @parameter property="mavanagaiata.baseDir"
     *            default-value="${project.basedir}"
     */
    protected File baseDir;

    /**
     * The flag to append to refs if there are changes in the index or working
     * tree
     *
     * @parameter property="mavanagaiata.dirtyFlag"
     * @since 0.4.0
     */
    protected String dirtyFlag = "-dirty";

    /**
     * The GIT_DIR path of the Git repository
     *
     * @parameter property="mavanagaiata.gitDir"yö
     */
    protected File gitDir;

    /**
     * The commit or ref to use as starting point for operations
     *
     * @parameter property="mavanagaiata.head"
     */
    protected String head = "HEAD";

    /**
     * Skip the plugin execution.
     *
     * @parameter default-value="false"
     */
    protected boolean skip = false;

    /**
     * Skip the plugin execution if outside a git repository.
     *
     * @parameter default-value="false"
     */
    protected boolean skipNoGit = false;

    /**
     * The Maven project
     *
     * @parameter property="project"
     * @readonly
     */
    protected MavenProject project;

    /**
     * The prefixes to prepend to property keys
     *
     * @parameter
     */
    protected String[] propertyPrefixes = { "mavanagaiata", "mvngit" };

    protected GitRepository repository;

    /**
     * Generic execution sequence for a Mavanagaiata mojo
     * <p>
     * Will initialize any needed resources, run the actual mojo code and
     * cleanup afterwards.
     *
     * @see #cleanup
     * @see #init
     * @see #run
     * @throws MojoExecutionException
     */
    public final void execute() throws MojoExecutionException {
        if (!skip) {
            boolean init = false;
            try {
                init = this.init();

                if (init) {
                    this.run();
                }
            } finally {
                if (init) {
                    this.cleanup();
                }
            }
        }
    }

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
     * Closes the JGit repository object
     *
     * @see Repository#close
     */
    protected void cleanup() {
        if (this.repository != null) {
            this.repository.close();
            this.repository = null;
        }
    }

    /**
     * Generic initialization for all Mavanagaiata mojos
     * <p>
     * This will initialize the JGit repository instance for further usage by
     * the mojo.
     *
     * @throws MojoExecutionException if the repository cannot be initialized
     */
    protected boolean init() throws MojoExecutionException {
        try {
            return this.initRepository();
        } catch (GitRepositoryException e) {
            throw new MojoExecutionException("Unable to initialize Mojo", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to initialize Mojo", e);
        }
    }

    /**
     * Initializes a JGit Repository object for further reference
     *
     * @see Repository
     * @throws IOException if retrieving information from the Git repository
     *         fails
     */
    protected boolean initRepository()
            throws GitRepositoryException, IOException,
                   MojoExecutionException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.readEnvironment();

        if (this.gitDir == null && this.baseDir == null) {
            throw new MojoExecutionException("Neither baseDir nor gitDir is set.");
        } else {
            if (this.baseDir != null && !this.baseDir.exists()) {
                if (skipNoGit) {
                    return false;
                }
                throw new FileNotFoundException("The baseDir " + this.baseDir + " does not exist");
            }
            if (this.gitDir != null && !this.gitDir.exists()) {
                if (skipNoGit) {
                    return false;
                }
                throw new FileNotFoundException("The gitDir " + this.gitDir + " does not exist");
            }
        }

        repositoryBuilder.setGitDir(this.gitDir);
        repositoryBuilder.setWorkTree(this.baseDir);
        this.repository = new JGitRepository(repositoryBuilder.build());
        if (!repository.check()) {
            if (skipNoGit) {
                return false;
            }
            throw new GitRepositoryException(baseDir + " is not a Git repository");
        }

        this.repository.setHeadRef(this.head);

        return true;
    }

    /**
     * The actual implementation of the mojo
     * <p>
     * This is called internally by {@link #init}.
     */
    protected abstract void run() throws MojoExecutionException;

}
