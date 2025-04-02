/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2025, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitRepository;

import static java.nio.file.Files.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        Path outputDir = createTempDirectory(null);
        outputDir.toFile().deleteOnExit();
        File outputFile = new File(outputDir.toString(), "output");

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
        Path outputDir = createTempDirectory(null);
        outputDir.toFile().deleteOnExit();
        deleteIfExists(outputDir);
        File outputFile = new File(outputDir.toString(), "output");

        PrintStream printStream = mock(PrintStream.class);
        mojo = spy(mojo);
        doReturn(printStream).when(mojo).createPrintStream();

        mojo.encoding = "someencoding";
        mojo.setOutputFile(outputFile);
        mojo.run(repository);

        assertThat(mojo.printStream, is(printStream));
    }

    @DisplayName("should throw the correct exception when the output directory cannot be created")
    @Test
    void testInitOutputFileCreateDirectoriesFailed() throws Exception {
        Path outputDir = createTempFile(null, null);
        outputDir.toFile().deleteOnExit();
        File outputFile = new File(outputDir.toString(), "output");

        PrintStream printStream = mock(PrintStream.class);
        mojo = spy(mojo);
        doReturn(printStream).when(mojo).createPrintStream();

        mojo.encoding = "someencoding";
        mojo.setOutputFile(outputFile);

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo(String.format("Could not create directory \"%s\" for output file.", outputDir))));
        assertThat(e.getCause(), is(instanceOf(FileAlreadyExistsException.class)));
    }

    @DisplayName("should throw the correct exception when the output file cannot be written")
    @Test
    void testInitOutputFileException() throws Exception {
        Path outputPath = createTempFile(null, null);
        File outputFile = outputPath.toFile();
        outputFile.deleteOnExit();

        mojo.encoding = "someencoding";
        mojo.setOutputFile(outputFile);

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo(String.format("Could not open output file \"%s\" for writing.", outputPath))));
        assertThat(e.getCause(), is(instanceOf(UnsupportedEncodingException.class)));
    }

    @DisplayName("should print a footer when configured")
    @Test
    void testGenerateOutputWithFooter() throws MavanagaiataMojoException {
        mojo.dateFormat = "'Some date'";
        mojo.footer = "Test footer";
        mojo.printStream = mock(PrintStream.class);
        mojo.generateOutput(repository);

        verify(mojo.printStream).printf("Test footer%n", VersionHelper.getVersion(), "Some date");
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

    static class GenericAbstractGitOutputMojo extends AbstractGitOutputMojo {

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
