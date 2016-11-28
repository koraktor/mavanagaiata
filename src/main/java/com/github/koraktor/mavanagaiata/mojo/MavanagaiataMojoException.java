/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

/**
 * A generic exception used to relay an error during mojo execution
 *
 * @author Sebastian Staudt
 * @since 0.6.0
 */
class MavanagaiataMojoException extends Exception {

    /**
     * Creates a new exception with the given message an cause
     * <p>
     * Additional arguments will be interpolated into the message with
     * {@link String#format(String, Object...)}.
     *
     * @param message The message used for the exception
     * @param cause The cause for this exception
     * @param args Additional arguments to interpolate into the message
     * @return A <code>MavanagaiataMojoException</code> created from the given
     *         parameters
     */
    static MavanagaiataMojoException create(String message, Throwable cause, Object... args) {
        String errorMessage = String.format(message, args);
        return new MavanagaiataMojoException(errorMessage, cause);
    }

    /**
     * Creates a new exception with the given message and cause
     *
     * @param message The message used for the exception
     * @param cause The cause for this exception
     */
    private MavanagaiataMojoException(String message, Throwable cause) {
        super(message, cause);
    }

}
