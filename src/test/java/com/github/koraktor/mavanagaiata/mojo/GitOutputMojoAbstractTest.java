/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 *
 * @author Sebastian Staudt
 */
abstract class GitOutputMojoAbstractTest<T extends AbstractGitOutputMojo> extends MojoAbstractTest<T> {

    protected ByteArrayOutputStream outputStream;

    protected BufferedReader reader;

    @Override
    public void setup() throws Exception {
        super.setup();

        this.mojo.footer = "Footer";

        this.outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(this.outputStream);
        this.mojo.outputStream = printStream;
    }

    protected void assertOutputLine(String line) throws IOException {
        if (this.reader == null) {
            this.reader = new BufferedReader(new StringReader(this.outputStream.toString()));
        }

        assertThat(this.reader.readLine(), is(equalTo(line)));
    }

}
