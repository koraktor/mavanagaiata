/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.FileUtils;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

/**
 * This goal generates the source code for a Java class with Git information
 * like commit ID and tag name.
 *
 * @author Sebastian Staudt
 * @since 0.5.0
 */
@Mojo(name ="info-class",
      defaultPhase = LifecyclePhase.GENERATE_SOURCES,
      threadSafe = true)
public class InfoClassMojo extends AbstractGitMojo {

    /**
     * The name of the class to generate
     */
    @Parameter(property = "mavanagaiata.info-class.className",
               defaultValue = "GitInfo")
    protected String className;

    /**
     * The encoding of the generated source file
     */
    @Parameter(property = "mavanagaiata.info-class.encoding",
               defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    @Component
    protected MavenFileFilter fileFilter;

    /**
     * The name of the package in which the class will be generated
     */
    @Parameter(property = "mavanagaiata.info-class.packageName",
               defaultValue = "${project.groupId}.${project.artifactId}")
    protected String packageName;

    /**
     * The directory to write the source code to
     * <p>
     * This directory is automatically added to the source roots used to
     * compile the project.
     */
    @Parameter(property = "mavanagaiata.info-class.outputDirectory",
               defaultValue = "${project.build.directory}/generated-sources/mavanagaiata")
    protected File outputDirectory;

    /**
     * The path to an alternative template for the info class
     */
    @Parameter(property = "mavanagaiata.info-class.templatePath")
    protected File templateFile;

    /**
     * Returns an input stream for the template source file for the info class
     * <p>
     * This may either be the builtin template or an arbitrary source file set
     * via {@code templatePath}.
     *
     * @return An input stream for the template source file
     * @throws MavanagaiataMojoException if the template source cannot be found
     */
    protected InputStream getTemplateSource() throws MavanagaiataMojoException {
        if (templateFile == null) {
            String templatePath = "TemplateGitInfoClass.java";
            return getClass().getResourceAsStream(templatePath);
        } else {
            try {
                return new FileInputStream(templateFile);
            } catch (FileNotFoundException e) {
                throw MavanagaiataMojoException.create(
                        "Info class template \"%s\" does not exist",
                        e,
                        templateFile.getAbsolutePath());
            }
        }
    }

    /**
     * Generates a info class filled providing information of the Git
     * repository
     *
     * @throws MavanagaiataMojoException if the info class cannot be generated
     */
    @Override
    public void run(GitRepository repository) throws MavanagaiataMojoException {
        this.addProperty("info-class.className", this.className);
        this.addProperty("info-class.packageName", this.packageName);

        try (InputStream templateStream = getTemplateSource()) {
            File tempSourceDir = File.createTempFile("mavanagaita-info-class", null);
            tempSourceDir.delete();
            tempSourceDir.mkdir();
            FileUtils.forceDeleteOnExit(tempSourceDir);
            String sourceFileName = this.className + ".java";
            File tempSourceFile = new File(tempSourceDir, sourceFileName);
            tempSourceFile.createNewFile();
            try (FileOutputStream tempSourceFileStream = new FileOutputStream(tempSourceFile)) {
                IOUtils.copy(templateStream, tempSourceFileStream);
            }

            final MapBasedValueSource valueSource = getValueSource(repository);
            FileUtils.FilterWrapper filterWrapper = new FileUtils.FilterWrapper() {
                @Override
                public Reader getReader(Reader fileReader) {
                    RegexBasedInterpolator regexInterpolator = new RegexBasedInterpolator();
                    regexInterpolator.addValueSource(valueSource);

                    return new InterpolatorFilterReader(fileReader,  regexInterpolator);
                }
            };

            List<FileUtils.FilterWrapper> filterWrappers = new ArrayList<>();
            filterWrappers.add(filterWrapper);

            File classDirectory = new File(this.outputDirectory, this.packageName.replace('.', '/'));
            classDirectory.mkdirs();
            File outputFile = new File(classDirectory, sourceFileName);
            outputFile.createNewFile();

            this.fileFilter.copyFile(tempSourceFile, outputFile, true, filterWrappers, this.encoding, true);
        } catch (GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Could not get all information from repository", e);
        } catch (IOException e) {
            throw MavanagaiataMojoException.create("Could not create temporary info class source", e);
        } catch (MavenFilteringException e) {
            e.printStackTrace();
        }

        this.project.addCompileSourceRoot(this.outputDirectory.getAbsolutePath());
    }

    protected MapBasedValueSource getValueSource(GitRepository repository)
            throws GitRepositoryException {
        GitTagDescription description = repository.describe();

        String abbrevId  = repository.getAbbreviatedCommitId();
        String shaId     = repository.getHeadCommit().getId();
        String describe  = description.toString();
        boolean isDirty  = repository.isDirty(this.dirtyIgnoreUntracked);
        String branch    = repository.getBranch();

        if (isDirty && this.dirtyFlag != null) {
            abbrevId += this.dirtyFlag;
            shaId    += this.dirtyFlag;
            describe += this.dirtyFlag;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        HashMap<String, String> values = new HashMap<>();
        values.put("BRANCH", branch);
        values.put("CLASS_NAME", this.className);
        values.put("COMMIT_ABBREV", abbrevId);
        values.put("COMMIT_SHA", shaId);
        values.put("DESCRIBE", describe);
        values.put("DIRTY", Boolean.toString(isDirty));
        values.put("PACKAGE_NAME", this.packageName);
        values.put("TAG_NAME", description.getNextTagName());
        values.put("TIMESTAMP", dateFormat.format(new Date()));
        values.put("VERSION", this.project.getVersion());

        String version = VersionHelper.getVersion();
        if (version != null) {
            values.put("MAVANAGAIATA_VERSION", version);
        }

        return new MapBasedValueSource(values);
    }

}
