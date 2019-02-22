/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2019, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;

@DisplayName("GitTagDescription")
class GitTagDescriptionTest {

    @DisplayName("should print a correct description")
    @Test
    void test() {
        GitTagDescription description = new GitTagDescription("deadbeef", "1.0.0", 3);

        assertThat(description.getNextTagName(), is(equalTo("1.0.0")));
        assertThat(description.isTagged(), is(false));
        assertThat(description.toString(), is(equalTo("1.0.0-3-gdeadbeef")));
    }

    @DisplayName("should print a correct description without a previous tag")
    @Test
    void testNoTag() {
        GitTagDescription description = new GitTagDescription("deadbeef", null, -1);

        assertThat(description.getNextTagName(), is(equalTo("")));
        assertThat(description.isTagged(), is(false));
        assertThat(description.toString(), is(equalTo("deadbeef")));
    }

    @DisplayName("should print a correct description if the commit is tagged")
    @Test
    void testTagged() {
        GitTagDescription description = new GitTagDescription("deadbeef", "1.0.0", 0);

        assertThat(description.getNextTagName(), is(equalTo("1.0.0")));
        assertThat(description.isTagged(), is(true));
        assertThat(description.toString(), is(equalTo("1.0.0")));
    }

}
