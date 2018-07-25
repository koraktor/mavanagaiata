/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.util.Date;
import java.util.TimeZone;

/**
 * This interface specifies the basic properties needed for the mojos to access
 * the information about a Git commit
 *
 * @author Sebastian Staudt
 */
public interface GitCommit {

    /**
     * Returns the date when this commit has been authored
     *
     * @return The author date of this commit
     */
    Date getAuthorDate();

    /**
     * Returns the email address of the author of this commit
     *
     * @return The commit author's email address
     */
    String getAuthorEmailAddress();

    /**
     * Returns the name of the author of this commit
     *
     * @return The commit author's name
     */
    String getAuthorName();

    /**
     * Returns the timezone in which this commit has been authored
     *
     * @return The author timezone of this commit
     */
    TimeZone getAuthorTimeZone();

    /**
     * Returns the date when this commit has been committed
     *
     * @return The committer date of this commit
     */
    Date getCommitterDate();

    /**
     * Returns the email address of the committer
     *
     * @return The committer's email address
     */
    String getCommitterEmailAddress();

    /**
     * Returns the name of the committer
     *
     * @return The committer's name
     */
    String getCommitterName();

    /**
     * Returns the timezone in which this commit has been committed
     *
     * @return The committer timezone of this commit
     */
    TimeZone getCommitterTimeZone();

    /**
     * Returns the SHA hash ID of this commit
     *
     * @return The SHA ID of this commit
     */
    String getId();

    /**
     * Returns the message of this commit
     *
     * @return The message of this commit
     */
    String getMessage();

    /**
     * Returns the subject of the commit's message
     * <p>
     * The message subject is the first line of the commit message.
     *
     * @return The message subject of this commit
     */
    String getMessageSubject();

    /**
     * @return {@code true} if this commit is a merge commit
     */
    boolean isMergeCommit();
}
