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
 * the information about a Git tag
 *
 * @author Sebastian Staudt
 */
public interface GitTag {

    /**
     * Returns the date when this tag has been created
     *
     * @return The creation date of this tag
     */
    Date getDate();

    /**
     * Returns the name of this tag
     *
     * @return The name of this tag
     */
    String getName();

    /**
     * Returns the timezone in which this tag has been created
     *
     * @return The timezone of this tag
     */
    TimeZone getTimeZone();

}
