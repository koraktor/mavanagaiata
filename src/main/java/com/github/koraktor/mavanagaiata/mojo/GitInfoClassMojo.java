/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.FileUtils;

/**
 * This goal generates the source code for a Java class with Git information
 * like commit ID and tag name.
 *
 * @author Sebastian Staudt
 * @goal info-class
 * @phase generate-sources
 * @requiresProject
 * @since 0.5.0
 */
public class GitInfoClassMojo extends AbstractGitMojo {

    /**
     * The name of the class to generate
     *
     * @parameter expression="${mavanagaiata.info-class.className}"
     */
    protected String className = "GitInfo";

    /**
     * The date format to use for the timestamp of the generated class
     *
     * @parameter property="mavanagaiata.info-class.dateFormat"
     */
    protected String dateFormat = baseDateFormat;

    /**
     * @parameter default-value="${project.build.sourceEncoding}"
     *            expression="${mavanagaiata.info-class.encoding}"
     */
    protected String encoding;

    /**
     * @component
     */
    protected MavenFileFilter fileFilter;

    /**
     * The name of the package in which the class will be generated
     *
     * @parameter default-value="${project.groupId}.${project.artifactId}"
     *            expression="${mavanagaiata.info-class.packageName}"
     */
    protected String packageName;

    /**
     * The directory to write the source code to
     *
     * @parameter default-value="${project.build.directory}/generated-sources/mavanagaiata"
     *            expression="${mavanagaiata.info-class.outputDirectory}"
     */
    protected File outputDirectory;

    /**
     * The path to an alternative template for the info class
     *
     * @parameter expression="${mavanagaiata.info-class.templatePath}"
     */
    protected File templateFile;

    /**
     * Generates a info class filled providing information of the Git
     * repository
     *
     * @throws MojoExecutionException if the info class cannot be generated
     */
    public void run() throws MojoExecutionException {
        this.addProperty("info-class.className", this.className);
        this.addProperty("info-class.packageName", this.packageName);

        InputStream templateStream;
        if (this.templateFile == null) {
            String templatePath = "TemplateGitInfoClass.java";
            templateStream = this.getClass().getResourceAsStream(templatePath);
        } else {
            try {
                templateStream = new FileInputStream(this.templateFile);
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException("Info class template does not exist", e);
            }
        }

        FileOutputStream tempSourceFileStream = null;
        try {
            File tempSourceDir = File.createTempFile("mavanagaita:info-class", null);
            tempSourceDir.delete();
            tempSourceDir.mkdir();
            FileUtils.forceDeleteOnExit(tempSourceDir);
            String sourceFileName = this.className + ".java";
            File tempSourceFile = new File(tempSourceDir, sourceFileName);
            tempSourceFile.createNewFile();
            tempSourceFileStream = new FileOutputStream(tempSourceFile);
            IOUtils.copy(templateStream, tempSourceFileStream);
            tempSourceFileStream.close();

            final MapBasedValueSource valueSource = this.getValueSource();
            FileUtils.FilterWrapper filterWrapper = new FileUtils.FilterWrapper() {
                public Reader getReader(Reader fileReader) {
                    RegexBasedInterpolator regexInterpolator = new RegexBasedInterpolator();
                    regexInterpolator.addValueSource(valueSource);

                    return new InterpolatorFilterReader(fileReader,  regexInterpolator);
                }
            };

            List<FileUtils.FilterWrapper> filterWrappers = new ArrayList<FileUtils.FilterWrapper>();
            filterWrappers.add(filterWrapper);

            File classDirectory = new File(this.outputDirectory, this.packageName.replace('.', '/'));
            classDirectory.mkdirs();
            File outputFile = new File(classDirectory, sourceFileName);
            outputFile.createNewFile();

            this.fileFilter.copyFile(tempSourceFile, outputFile, true, filterWrappers, this.encoding, true);
        } catch (GitRepositoryException e) {
            throw new MojoExecutionException("Could not get all information from repository", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create temporary info class source", e);
        } catch (MavenFilteringException e) {
            e.printStackTrace();
        } finally {
            try {
                if (templateStream != null) {
                    templateStream.close();
                }
                if (tempSourceFileStream != null) {
                    tempSourceFileStream.close();
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Could not close temporary info class source");
            }
        }

        this.project.addCompileSourceRoot(this.outputDirectory.getAbsolutePath());

        this.cleanup();
    }

    protected MapBasedValueSource getValueSource()
            throws GitRepositoryException {
        GitTagDescription description = this.repository.describe();
        SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("CLASS_NAME", this.className);
        values.put("COMMIT_ABBREV", this.repository.getAbbreviatedCommitId());
        values.put("COMMIT_SHA", this.repository.getHeadCommit().getId());
        values.put("DESCRIBE", description.toString());
        values.put("PACKAGE_NAME", this.packageName);
        values.put("TAG_NAME", description.getNextTagName());
        values.put("TIMESTAMP", dateFormat.format(new Date()));
        values.put("VERSION", this.project.getVersion());

        return new MapBasedValueSource(values);
    }

}
