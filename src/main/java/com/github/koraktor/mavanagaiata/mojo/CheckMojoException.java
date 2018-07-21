/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

/**
 * Exception type thrown during repository checks
 *
 * @author Sebastian Staudt
 * @since 0.8.0
 * @see CheckMojo
 */
class CheckMojoException extends MavanagaiataMojoException {

    /**
     * Types of failures
     */
    enum Type {
        UNTAGGED, UNCLEAN, WRONG_COMMIT_MSG, WRONG_BRANCH
    }

    /**
     * Returns a message for the given type and (optional) arguments
     *
     * @param type The type of exception
     * @param args The arguments to integrate into the error message
     * @return A formatted exception message
     */
    private static String getMessageForType(Type type, String... args) {
        String message = null;

        switch (type) {
            case UNCLEAN:
                message = "The worktree is in an unclean state. Please stash " +
                        "or commit your changes.";
                break;
            case UNTAGGED:
                message = "The current commit is not tagged.";
                break;
            case WRONG_BRANCH:
                message = "The current branch is `%s`, but builds are only " +
                        "allowed from `%s`.";
                break;
            case WRONG_COMMIT_MSG:
                message = "The commit message does not match `%s`.";
        }

        return String.format(message, (Object[]) args);
    }

    Type type;

    /**
     * Creates a new exception for the given check failure type
     *
     * @param type The type of failure
     * @param args The arguments to integrate into the error message
     */
    CheckMojoException(Type type, String... args) {
        super(getMessageForType(type, args), null);

        this.type = type;
    }

    @Override
    boolean isGraceful() {
        return true;
    }

}
