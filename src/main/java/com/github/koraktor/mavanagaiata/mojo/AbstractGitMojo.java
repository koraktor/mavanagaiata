/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.jgit.JGitRepository;

/**
 * This abstract Mojo implements initializing a JGit Repository and provides
 * this Repository instance to subclasses.
 *
 * @author Sebastian Staudt
 * @see GitRepository
 * @since 0.1.0
 */
abstract class AbstractGitMojo extends AbstractMojo {

    /**
     * The date format to use for various dates
     */
    @Parameter(property = "mavanagaiata.dateFormat",
               defaultValue = "MM/dd/yyyy hh:mm a Z")
    protected String dateFormat;

    /**
     * The working tree of the Git repository.
     * <p>
     * If there is only one project inside the Git repository this is probably
     * {@code ${project.basedir}} (default).
     * <p>
     * <strong>Note:</strong> The {@code GIT_DIR} can be found automatically
     * even if this is not the real working tree but one of its subdirectories.
     * But Mavanagaiata cannot determine the state of the working tree (e.g.
     * for the dirty flag) if this is not set correctly.
     */
    @Parameter(property = "mavanagaiata.baseDir",
               defaultValue = "${project.basedir}")
    protected File baseDir;

    /**
     * The flag to append to refs if there are changes in the index or working
     * tree
     * <p>
     * Setting this to either {@code "false"} or {@code "null"} will disable
     * flagging refs as dirty.
     *
     * @since 0.4.0
     */
    @Parameter(property = "mavanagaiata.dirtyFlag",
               defaultValue = "-dirty")
    protected String dirtyFlag;

    /**
     * Specifies if the dirty flag should also be appended if there are
     * untracked files
     * <p>
     * If {@code false} only modified files that are already known to Git will
     * cause the dirty flag to be appended.
     *
     * @since 0.5.0
     */
    @Parameter(property = "mavanagaiata.dirtyIgnoreUntracked",
               defaultValue = "false")
    protected boolean dirtyIgnoreUntracked;

    /**
     * Specifies if a failed execution of the mojo will stop the build process
     * <p>
     * If {@code true} a failure during mojo execution will not stop the build
     * process.
     *
     * @since 0.6.0
     */
    @Parameter(property = "mavanagaiata.failGracefully",
               defaultValue = "false")
    protected boolean failGracefully = false;

    /**
     * The {@code GIT_DIR} path of the Git repository
     * <p>
     * <strong>Warning:</strong> Do not set this when you don't have a good
     * reason to do so. The {@code GIT_DIR} can be found automatically if your
     * project resides somewhere in a usual Git repository.
     */
    @Parameter(property = "mavanagaiata.gitDir")
    protected File gitDir;

    /**
     * The commit or ref to use as starting point for operations
     */
    @Parameter(property = "mavanagaiata.head",
               defaultValue = GitRepository.DEFAULT_HEAD)
    protected String head;

    /**
     * Skip the plugin execution
     *
     * @since 0.5.0
     */
    @Parameter(property = "mavanagaiata.skip",
               defaultValue = "false")
    protected boolean skip = false;

    /**
     * Skip the plugin execution if not inside a Git repository
     *
     * @since 0.5.0
     */
    @Parameter(property = "mavanagaiata.skipNoGit",
               defaultValue = "false")
    protected boolean skipNoGit = false;

    /**
     * The Maven project
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * The prefixes to prepend to property keys
     */
    @Parameter(property = "mavanagaiata.propertyPrefixes",
                defaultValue = "mavanagaiata,mvngit")
    protected String[] propertyPrefixes = { "mavanagaiata", "mvngit" };

    /**
     * Generic execution sequence for a Mavanagaiata mojo
     * <p>
     * Will initialize any needed resources, run the actual mojo code and
     * cleanup afterwards.
     *
     * @see #init
     * @see #run
     * @throws MojoExecutionException if the mojo execution fails and
     *         {@code failGracefully} is {@code false}
     * @throws MojoFailureException if the mojo execution fails and
     *         {@code failGracefully} is {@code true}
     */
    public final void execute()
            throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }

        try (GitRepository repository = init()) {
            if (repository != null) {
                run(repository);
            }
        } catch (MavanagaiataMojoException e) {
            if (failGracefully || e.isGraceful()) {
                throw new MojoFailureException(e.getMessage(), e);
            }

            throw new MojoExecutionException(e.getMessage(), e);
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
     * Generic initialization for all Mavanagaiata mojos
     * <p>
     * This will initialize the JGit repository instance for further usage by
     * the mojo.
     *
     * @return {@code false} if the execution should be skipped
     * @throws MavanagaiataMojoException if the repository cannot be initialized
     */
    protected GitRepository init() throws MavanagaiataMojoException {
        try {
            prepareParameters();
            GitRepository repository = initRepository();

            if (repository.isOnUnbornBranch()) {
                getLog().warn("Building from an unborn branch. Skipping…");

                return null;
            }

            return repository;
        } catch (GitRepositoryException e) {
            if (this.skipNoGit) {
                return null;
            }
            throw MavanagaiataMojoException.create("Unable to initialize Git repository", e);
        }
    }

    /**
     * Initializes a JGit Repository object for further reference
     *
     * @return The repository instance
     * @throws GitRepositoryException if retrieving information from the Git
     *         repository fails
     */
    protected GitRepository initRepository() throws GitRepositoryException {
        GitRepository repository = new JGitRepository(baseDir, gitDir);
        repository.check();
        repository.setHeadRef(head);

        return repository;
    }

    /**
     * Prepares and validates user-supplied parameters
     */
    protected void prepareParameters() {
        if (this.dirtyFlag.equals("false") ||
                this.dirtyFlag.equals("null")) {
            this.dirtyFlag = null;
        }
    }

    /**
     * The actual implementation of the mojo
     *
     * @param repository The repository instance to use
     * @throws MavanagaiataMojoException if there is an error during execution
     */
    protected abstract void run(GitRepository repository) throws MavanagaiataMojoException;

}
