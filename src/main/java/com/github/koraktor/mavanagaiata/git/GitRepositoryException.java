/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

/**
 * An exception that can be thrown during Git repository actions
 *
 * @author Sebastian Staudt
 */
public class GitRepositoryException extends Exception {

    /**
     * Create a new exception instance with the given message
     *
     * @param message The message of the exception
     */
    public GitRepositoryException(String message) {
        super(message);
    }

    /**
     * Create a new exception instance with the given message and cause
     *
     * @param message The message of the exception
     * @param cause The cause of the exception
     */
    public GitRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
