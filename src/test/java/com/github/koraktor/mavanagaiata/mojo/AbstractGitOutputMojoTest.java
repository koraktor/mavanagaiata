/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractGitOutputMojoTest extends MojoAbstractTest<AbstractGitOutputMojo> {

    private GenericAbstractGitOutputMojo genericMojo() {
        return (GenericAbstractGitOutputMojo) mojo;
    }

    @Before
    @Override
    public void setup() {
        mojo = new GenericAbstractGitOutputMojo();
        mojo.dateFormat = "MM/dd/yyyy hh:mm a Z";
        mojo.footer = "";
    }

    @Test
    public void testInitStdout() throws Exception {
        mojo.setOutputFile(null);
        mojo.run(repository);

        assertThat(genericMojo().printStream, is(System.out));
    }

    @Test
    public void testInitOutputFile() throws Exception {
        File outputFile = mock(File.class);
        File parentFile = mock(File.class);
        when(parentFile.exists()).thenReturn(true);
        when(parentFile.isDirectory()).thenReturn(true);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        PrintStream printStream = mock(PrintStream.class);
        mojo = spy(mojo);
        doReturn(printStream).when(mojo).createPrintStream();

        this.mojo.encoding = "someencoding";
        this.mojo.setOutputFile(outputFile);
        this.mojo.run(repository);

        assertThat(genericMojo().printStream, is(printStream));
    }

    @Test
    public void testInitOutputFileCreateDirectories() throws Exception {
        File outputFile = mock(File.class);
        File parentFile = mock(File.class);
        when(parentFile.exists()).thenReturn(false);
        when(parentFile.mkdirs()).thenReturn(true);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        PrintStream printStream = mock(PrintStream.class);
        mojo = spy(mojo);
        doReturn(printStream).when(mojo).createPrintStream();

        mojo.encoding = "someencoding";
        mojo.setOutputFile(outputFile);
        mojo.run(repository);

        assertThat(((GenericAbstractGitOutputMojo) mojo).printStream, is(printStream));
    }

    @Test
    public void testInitOutputFileCreateDirectoriesFailed() throws Exception {
        File outputFile = mock(File.class);
        File parentFile = mock(File.class);
        when(parentFile.exists()).thenReturn(false);
        when(parentFile.getAbsolutePath()).thenReturn("/some");
        when(parentFile.mkdirs()).thenReturn(false);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        PrintStream printStream = mock(PrintStream.class);
        mojo = spy(mojo);
        doReturn(printStream).when(mojo).createPrintStream();

        mojo.encoding = "someencoding";
        mojo.setOutputFile(outputFile);

        try {
            mojo.run(repository);
        } catch (Exception e) {
            assertThat(e, is(instanceOf(MavanagaiataMojoException.class)));
            assertThat(e.getMessage(), is(equalTo("Could not create directory \"/some\" for output file.")));
            assertThat(e.getCause(), is(nullValue()));
        }
    }

    @Test
    public void testInitOutputFileException() throws Exception {
        FileNotFoundException fileNotFoundException = mock(FileNotFoundException.class);
        File outputFile = mock(File.class);
        when(outputFile.getAbsolutePath()).thenReturn("/some/file");
        File parentFile = mock(File.class);
        when(parentFile.exists()).thenReturn(true);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        mojo = spy(mojo);
        doThrow(fileNotFoundException).when(mojo).createPrintStream();

        this.mojo.encoding = "someencoding";
        this.mojo.setOutputFile(outputFile);

        try {
            mojo.run(repository);
            fail("No exception thrown.");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(MavanagaiataMojoException.class)));
            assertThat(e.getMessage(), is(equalTo("Could not open output file \"/some/file\" for writing.")));
            assertThat(e.getCause(), is((Throwable) fileNotFoundException));
        }
    }

    @Test
    public void testGenerateOutputWithFooter() throws MavanagaiataMojoException {
        PrintStream printStream = mock(PrintStream.class);

        this.mojo.footer = "Test footer";
        this.mojo.generateOutput(repository, printStream);

        verify(printStream).println("Test footer");
        verify(printStream).flush();
    }

    @Test
    public void testGenerateOutputWithoutFooter() throws MavanagaiataMojoException {
        PrintStream printStream = mock(PrintStream.class);

        mojo.generateOutput(repository, printStream);

        verify(printStream, never()).println();
        verify(printStream).flush();
    }

    class GenericAbstractGitOutputMojo extends AbstractGitOutputMojo {

        File outputFile;

        PrintStream printStream;

        public File getOutputFile() {
            return this.outputFile;
        }

        public void setOutputFile(File outputFile) {
            this.outputFile = outputFile;
        }

        protected void writeOutput(GitRepository repository, PrintStream printStream) {
            this.printStream = printStream;
        }

    }

}
