/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.jgit.JGitRepository;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 *
 *
 * @author Sebastian Staudt
 */
abstract class GitOutputMojoAbstractTest<T extends AbstractGitOutputMojo> extends MojoAbstractTest<T> {

    private ByteArrayOutputStream outputStream;

    PrintStream printStream;

    private BufferedReader reader;

    @Override
    public void setup() throws Exception {
        super.setup();

        mojo.footer = "Footer";

        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
    }

    void assertOutputLine(String line) throws IOException {
        if (this.reader == null) {
            this.reader = new BufferedReader(new StringReader(this.outputStream.toString()));
        }

        assertThat(this.reader.readLine(), is(equalTo(line)));
    }

    @Override
    protected void testError(String errorMessage) {
        try {
            repository = mock(JGitRepository.class, new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    throw new GitRepositoryException("");
                }
            });
            mojo.generateOutput(repository, printStream);
            fail("No exception thrown.");
        } catch(Exception e) {
            assertThat(e, is(instanceOf(MavanagaiataMojoException.class)));
            assertThat(e.getMessage(), is(equalTo(errorMessage)));
            assertThat(e.getCause(), is(instanceOf(GitRepositoryException.class)));
        }
    }
}
