package com.github.koraktor.mavanagaiata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.Test;

public abstract class AbstractGitOutputMojoTest<T extends AbstractGitOutputMojo>
        extends AbstractMojoTest<T> {

    protected BufferedReader reader;

    protected abstract void assertOutput() throws IOException;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        File tempFile = File.createTempFile("output", null);
        this.mojo.outputFile = tempFile;
        this.reader = new BufferedReader(new FileReader(tempFile));
    }

    @Override
    public void tearDown() throws IOException {
        this.reader.close();
        if(this.mojo.outputFile != null && !this.mojo.outputFile.delete()) {
            this.mojo.outputFile.deleteOnExit();
        }
    }

    @Test
    public void testNonExistantDirectory() throws Exception {
        this.reader.close();
        if(!this.mojo.outputFile.delete()) {
            this.mojo.outputFile.deleteOnExit();
        }
        File tempDir  = File.createTempFile("temp", null);
        tempDir.delete();
        tempDir.deleteOnExit();
        File tempFile = new File(tempDir + "/output");
        this.mojo.outputFile = tempFile;
        this.mojo.execute();

        this.reader = new BufferedReader(new FileReader(tempFile));

        this.assertOutput();
    }

    @Test
    public void testResult() throws Exception {
        this.mojo.execute();

        this.assertOutput();
    }

    @Test
    public void testSetOutputFile() {
        File file = new File("./test");
        this.mojo.setOutputFile(file);
        assertEquals(file, this.mojo.outputFile);
    }

    @Test
    public void testStdOut() throws Exception {
        try {
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(oStream);
            System.setOut(stream);

            this.reader.close();
            if(!this.mojo.outputFile.delete()) {
                this.mojo.outputFile.deleteOnExit();
            }
            this.mojo.outputFile = null;
            this.mojo.execute();

            byte[] output = oStream.toByteArray();
            this.reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output)));

            this.assertOutput();
        } finally {
            System.setOut(null);
        }
    }

}
