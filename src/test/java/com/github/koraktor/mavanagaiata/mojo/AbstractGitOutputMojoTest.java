/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractGitOutputMojo.class)
public class AbstractGitOutputMojoTest extends MojoAbstractTest<AbstractGitOutputMojo> {

    @Before
    @Override
    public void setup() {
        this.mojo = new GenericAbstractGitOutputMojo();
        this.mojo.dateFormat = "MM/dd/yyyy hh:mm a Z";
    }

    @Test
    public void testCleanupOutputFile() {
        this.mojo.outputStream = mock(PrintStream.class);
        File outputFile = mock(File.class);

        this.mojo.setOutputFile(outputFile);
        this.mojo.cleanup();

        verify(this.mojo.outputStream).flush();
        verify(this.mojo.outputStream).close();
    }

    @Test
    public void testCleanupStdout() {
        this.mojo.outputStream = mock(PrintStream.class);

        this.mojo.setOutputFile(null);
        this.mojo.cleanup();

        verify(this.mojo.outputStream).flush();
        verifyNoMoreInteractions(this.mojo.outputStream);
    }

    @Test
    public void testInitStdout() throws Exception {
        this.mojo.setOutputFile(null);
        this.mojo.initOutputStream();

        assertThat(this.mojo.outputStream, is(System.out));
    }

    @Test
    public void testInitOutputFile() throws Exception {
        File outputFile = mock(File.class);
        File parentFile = mock(File.class);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        when(parentFile.exists()).thenReturn(true);
        PrintStream printStream = mock(PrintStream.class);
        whenNew(PrintStream.class).withArguments(outputFile, "someencoding")
            .thenReturn(printStream);

        this.mojo.encoding = "someencoding";
        this.mojo.setOutputFile(outputFile);
        this.mojo.initOutputStream();

        assertThat(this.mojo.outputStream, is(printStream));
    }

    @Test
    public void testInitOutputFileCreateDirectories() throws Exception {
        File outputFile = mock(File.class);
        File parentFile = mock(File.class);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        when(parentFile.exists()).thenReturn(false);
        PrintStream printStream = mock(PrintStream.class);
        whenNew(PrintStream.class).withArguments(outputFile, "someencoding")
            .thenReturn(printStream);

        this.mojo.encoding = "someencoding";
        this.mojo.setOutputFile(outputFile);
        this.mojo.initOutputStream();

        assertThat(this.mojo.outputStream, is(printStream));

        verify(parentFile).mkdirs();
    }

    @Test
    public void testInitOutputFileException() throws Exception {
        IOException ioException = mock(IOException.class);
        File outputFile = mock(File.class);
        when(outputFile.getAbsolutePath()).thenReturn("/some/file");
        File parentFile = mock(File.class);
        when(outputFile.getParentFile()).thenReturn(parentFile);
        when(parentFile.exists()).thenReturn(true);
        whenNew(PrintStream.class).withArguments(outputFile, "someencoding")
            .thenThrow(ioException);

        this.mojo.encoding = "someencoding";
        this.mojo.setOutputFile(outputFile);

        try {
            this.mojo.initOutputStream();
            fail("No exception thrown.");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(MavanagaiataMojoException.class)));
            assertThat(e.getMessage(), is(equalTo("Could not open output file \"/some/file\" for writing.")));
            assertThat(e.getCause(), is((Throwable) ioException));
        }
    }

    @Test
    public void testInsertFooter() {
        this.mojo.outputStream = mock(PrintStream.class);

        this.mojo.footer = "Test footer";
        this.mojo.insertFooter();

        verify(this.mojo.outputStream).println("Test footer");
    }

    @Test
    public void testInsertEmptyFooter() {
        this.mojo.outputStream = mock(PrintStream.class);

        this.mojo.footer = "";
        this.mojo.insertFooter();

        verifyZeroInteractions(this.mojo.outputStream);
    }

    class GenericAbstractGitOutputMojo extends AbstractGitOutputMojo {

        protected File outputFile;

        public File getOutputFile() {
            return this.outputFile;
        }

        public void setOutputFile(File outputFile) {
            this.outputFile = outputFile;
        }

        protected void run() throws MavanagaiataMojoException {}

    }

}
