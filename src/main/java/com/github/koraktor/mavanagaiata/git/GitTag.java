/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2026, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.time.Instant;
import java.time.ZoneId;

/**
 * This interface specifies the basic properties needed for the mojos to access
 * the information about a Git tag
 *
 * @author Sebastian Staudt
 */
public interface GitTag {

    /**
     * Returns the instant when this tag has been created
     *
     * @return The creation instant of this tag
     */
    Instant getInstant();

    /**
     * Returns the name of this tag
     *
     * @return The name of this tag
     */
    String getName();

    /**
     * Returns ID of the timezone in which this tag has been created
     *
     * @return The timezone ID of this tag
     */
    ZoneId getTimeZoneId();

    /**
     * @return {@code true}, if this tag’s metadata has been loaded
     * @see GitRepository#loadTag
     */
    boolean isLoaded();
}
