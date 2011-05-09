package com.github.koraktor.mavanagaiata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * This abstract Mojo implements writing output to a <code>PrintStream</code>
 *
 * This is either <code>System.out</code> by default, but maybe another
 * <code>PrintStream</code> object wrapped around a file given by
 * <code>outputFile</code>.
 *
 * @author Sebastian Staudt
 * @see File
 * @see PrintStream
 */
public abstract class AbstractGitOutputMojo extends AbstractGitMojo {

    /**
     * The file to write the changelog to
     *
     * @parameter expression="${mavanagaiata.changelog.outputFile}"
     */
    protected File outputFile;

    protected PrintStream outputStream;

    /**
     * Flushes the <code>PrintStream</code> and closes it if it is not
     * <code>System.out</code>
     */
    protected void closeOutputStream() {
        if(this.outputStream != null) {
            this.outputStream.flush();
            if(this.outputFile != null) {
                this.outputStream.close();
            }
        }
    }

    /**
     * Initializes the <code>PrintStream</code> to use
     *
     * This is <code>System.out</code> if no output file is given (default).
     * Otherwise the parent directories of <code>outputFile</code> are created
     * and a new <code>PrintStream</code> for that file is created.
     *
     * @throws FileNotFoundException if the file specified by
     *         <code>outputFile</code> cannot be found
     */
    protected void initOutputStream() throws FileNotFoundException {
        if(this.outputFile == null) {
            this.outputStream = System.out;
        } else {
            if(!this.outputFile.getParentFile().exists()) {
                this.outputFile.getParentFile().mkdirs();
            }
            this.outputStream = new PrintStream(this.outputFile);
        }
    }

}
