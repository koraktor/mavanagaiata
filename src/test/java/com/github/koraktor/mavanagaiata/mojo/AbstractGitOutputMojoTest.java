/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
@DisplayName("AbstractGitOutputMojo")
class AbstractGitOutputMojoTest extends MojoAbstractTest<AbstractGitOutputMojo> {

    private GenericAbstractGitOutputMojo genericMojo() {
        return (GenericAbstractGitOutputMojo) mojo;
    }

    @BeforeEach
    @Override
    public void setup() {
        mojo = new GenericAbstractGitOutputMojo();
        mojo.dateFormat = "MM/dd/yyyy hh:mm a Z";
        mojo.footer = "";
    }

    @DisplayName("should use standard out when no file is given")
    @Test
    void testInitStdout() throws Exception {
        mojo.setOutputFile(null);
        mojo.run(repository);

        assertThat(mojo.printStream, is(System.out));
    }

    @DisplayName("should create a print stream for the output file")
    @Test
    void testInitOutputFile() throws Exception {
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

    @DisplayName("should create output directories if necessary")
    @Test
    void testInitOutputFileCreateDirectories() throws Exception {
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

    @DisplayName("should throw the correct exception when the output directory cannot be created")
    @Test
    void testInitOutputFileCreateDirectoriesFailed() throws Exception {
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

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo("Could not create directory \"/some\" for output file.")));
        assertThat(e.getCause(), is(nullValue()));
    }

    @DisplayName("should throw the correct exception when the output file cannot be written")
    @Test
    void testInitOutputFileException() throws Exception {
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

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo("Could not open output file \"/some/file\" for writing.")));
        assertThat(e.getCause(), is(fileNotFoundException));
    }

    @DisplayName("should print a footer when configured")
    @Test
    void testGenerateOutputWithFooter() throws MavanagaiataMojoException {
        mojo.footer = "Test footer";
        mojo.printStream = mock(PrintStream.class);
        mojo.generateOutput(repository);

        verify(mojo.printStream).println("Test footer");
        verify(mojo.printStream).flush();
    }

    @DisplayName("should add a newline at the end if no footer is configured")
    @Test
    void testGenerateOutputWithoutFooter() throws MavanagaiataMojoException {
        mojo.printStream = mock(PrintStream.class);
        mojo.generateOutput(repository);

        verify(mojo.printStream, never()).println();
        verify(mojo.printStream).flush();
    }

    class GenericAbstractGitOutputMojo extends AbstractGitOutputMojo {

        File outputFile;

        public File getOutputFile() {
            return this.outputFile;
        }

        public void setOutputFile(File outputFile) {
            this.outputFile = outputFile;
        }

        protected void writeOutput(GitRepository repository) {}

    }

}
