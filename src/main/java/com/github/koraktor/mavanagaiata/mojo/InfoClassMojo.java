/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2025, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.FilterWrapper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;

import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static org.apache.commons.io.FileUtils.*;

/**
 * This goal generates the source code for a Java class with Git information
 * like commit ID and tag name.
 *
 * @author Sebastian Staudt
 * @since 0.5.0
 */
@Mojo(name = "info-class",
      defaultPhase = LifecyclePhase.GENERATE_SOURCES,
      threadSafe = true)
public class InfoClassMojo extends AbstractGitMojo {

    private static final String BUILTIN_TEMPLATE_PATH = "TemplateGitInfoClass.java";

    /**
     * The name of the class to generate
     */
    @Parameter(property = "mavanagaiata.info-class.className",
               defaultValue = "GitInfo")
    String className;

    /**
     * The encoding of the generated source file
     */
    @Parameter(property = "mavanagaiata.info-class.encoding",
               defaultValue = "${project.build.sourceEncoding}")
    String encoding;

    @Component
    MavenFileFilter fileFilter;

    /**
     * The name of the package in which the class will be generated
     */
    @Parameter(property = "mavanagaiata.info-class.packageName",
               defaultValue = "${project.groupId}.${project.artifactId}")
    String packageName;

    /**
     * The directory to write the source code to
     * <p>
     * This directory is automatically added to the source roots used to
     * compile the project.
     */
    @Parameter(property = "mavanagaiata.info-class.outputDirectory",
               defaultValue = "${project.build.directory}/generated-sources/mavanagaiata")
    File outputDirectory;

    /**
     * The path to an alternative template for the info class
     */
    @Parameter(property = "mavanagaiata.info-class.templatePath")
    private File templateFile;

    /**
     * Returns an input stream for the template source file for the info class
     * <p>
     * This may either be the builtin template or an arbitrary source file set
     * via {@code templatePath}.
     *
     * @return An input stream for the template source file
     * @throws FileNotFoundException if the template source cannot be found
     */
    InputStream getTemplateSource() throws IOException {
        if (templateFile == null) {
            return InfoClassMojo.class.getResourceAsStream(BUILTIN_TEMPLATE_PATH);
        } else {
            return newInputStream(templateFile.toPath());
        }
    }

    /**
     * Generates an info class filled providing information of the Git
     * repository
     *
     * @throws MavanagaiataMojoException if the info class cannot be generated
     */
    @Override
    public void run(GitRepository repository) throws MavanagaiataMojoException {
        addProperty("info-class.className", className);
        addProperty("info-class.packageName", packageName);

        try {
            File sourceFile = copyTemporaryTemplate();
            writeSourceFile(repository, sourceFile);
        } catch (GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Could not get all information from repository", e);
        } catch (IOException | MavenFilteringException e) {
            throw MavanagaiataMojoException.create("Could not create info class source", e);
        }

        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }

    private File copyTemporaryTemplate()
            throws IOException, MavanagaiataMojoException {
        try (InputStream templateStream = getTemplateSource()) {
            File tempSourceDir;
            try {
                tempSourceDir = createTempDirectory("mavanagaiata-info-class").toFile();
            } catch (IOException e) {
                throw MavanagaiataMojoException.create("Could not create temporary directory", e);
            }
            forceDeleteOnExit(tempSourceDir);
            String sourceFileName = className + ".java";
            File tempSourceFile = new File(tempSourceDir, sourceFileName);
            if (!tempSourceFile.createNewFile()) {
                throw MavanagaiataMojoException.create("Could not create temporary file %s", null, tempSourceFile.getAbsolutePath());
            }
            try (FileOutputStream tempSourceFileStream = new FileOutputStream(tempSourceFile)) {
                IOUtils.copy(templateStream, tempSourceFileStream);
            }

            return tempSourceFile;
        }
    }

    MapBasedValueSource getValueSource(GitRepository repository)
            throws GitRepositoryException {
        GitTagDescription description = repository.describe();

        String abbrevId  = repository.getAbbreviatedCommitId();
        String shaId     = repository.getHeadCommit().getId();
        String describe  = description.toString();
        boolean isDirty  = repository.isDirty(dirtyIgnoreUntracked);

        if (isDirty && dirtyFlag != null) {
            abbrevId += dirtyFlag;
            shaId    += dirtyFlag;
            describe += dirtyFlag;
        }

        HashMap<String, String> values = new HashMap<>();
        values.put("BRANCH", repository.getBranch());
        values.put("CLASS_NAME", className);
        values.put("COMMIT_ABBREV", abbrevId);
        values.put("COMMIT_SHA", shaId);
        values.put("DESCRIBE", describe);
        values.put("DIRTY", Boolean.toString(isDirty));
        values.put("PACKAGE_NAME", packageName);
        values.put("TAG_NAME", description.getNextTagName());
        values.put("TIMESTAMP", new SimpleDateFormat(dateFormat).format(new Date()));
        values.put("VERSION", project.getVersion());

        String version = VersionHelper.getVersion();
        if (version != null) {
            values.put("MAVANAGAIATA_VERSION", version);
        }

        return new MapBasedValueSource(values);
    }

    private void writeSourceFile(GitRepository repository, File sourceFile)
            throws GitRepositoryException, MavanagaiataMojoException,
                   MavenFilteringException {
        File packageDirectory = new File(outputDirectory, packageName.replace('.', '/'));
        File outputFile = new File(packageDirectory, sourceFile.getName());

        try {
            deleteIfExists(outputFile.toPath());
            createDirectories(packageDirectory.toPath());

            List<FilterWrapper> filterWrappers = singletonList(new ValueSourceFilter(getValueSource(repository)));
            fileFilter.copyFile(sourceFile, outputFile, true, filterWrappers, encoding, true);
        } catch (IOException e) {
            throw MavanagaiataMojoException.create("Could not create class source: %s", e, outputFile.getAbsolutePath());
        }
    }

    private static class ValueSourceFilter extends FilterWrapper {
        private final ValueSource valueSource;

        ValueSourceFilter(ValueSource valueSource) {
            this.valueSource = valueSource;
        }

        @Override
        public Reader getReader(Reader fileReader) {
            RegexBasedInterpolator regexInterpolator = new RegexBasedInterpolator();
            regexInterpolator.addValueSource(valueSource);

            return new InterpolatorFilterReader(fileReader,  regexInterpolator);
        }
    }
}
