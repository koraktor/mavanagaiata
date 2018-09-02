/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.maven.shared.filtering.MavenFileFilter;

import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
@DisplayName("InfoClassMojo")
class InfoClassMojoTest extends MojoAbstractTest<InfoClassMojo> {

    private Date timestamp;

    @BeforeEach
    public void setup() throws Exception{
        super.setup();

        mojo.className       = "GitInfo";
        mojo.encoding        = "UTF-8";
        mojo.fileFilter      = mock(MavenFileFilter.class);
        mojo.packageName     = "com.github.koraktor.mavanagaita";
        mojo.outputDirectory = File.createTempFile("mavanagaiata-tests", null);
        if (!(mojo.outputDirectory.delete() && mojo.outputDirectory.mkdirs())) {
            fail("Unable to create output directory.");
        }

        when(mojo.project.getVersion()).thenReturn("1.2.3");

        when(repository.getAbbreviatedCommitId()).thenReturn("deadbeef");
        GitTagDescription description = mock(GitTagDescription.class);
        when(description.getNextTagName()).thenReturn("v1.2.3");
        when(description.toString()).thenReturn("v1.2.3-4-gdeadbeef");
        when(repository.describe()).thenReturn(description);
        when(repository.getBranch()).thenReturn("master");
        when(repository.getHeadCommit().getId()).thenReturn("deadbeefdeadbeefdeadbeefdeadbeef");

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        timestamp = calendar.getTime();
    }

    @DisplayName("should handle errors while creating the source file")
    @Test
    void testFailureCreateSource() throws Exception {
        Throwable exception = new FileNotFoundException();
        mojo = spy(mojo);
        when(mojo.getTemplateSource()).thenThrow(exception);

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo("Could not create info class source")));
    }

    @DisplayName("should handle errors while reading the repository")
    @Test
    void testFailureRepository() throws Exception {
        Throwable exception = new GitRepositoryException("");
        when(repository.describe()).thenThrow(exception);

        MavanagaiataMojoException e = assertThrows(MavanagaiataMojoException.class,
            () -> mojo.run(repository));
        assertThat(e.getMessage(), is(equalTo("Could not get all information from repository")));
    }

    @DisplayName("should provide a MapBasedValueSource with all information")
    @Test
    void testGetValueSource() throws Exception {
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("false")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(dateFormat.parse(valueSource.getValue("TIMESTAMP").toString()), is(greaterThanOrEqualTo(timestamp)));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @DisplayName("should handle dirty worktrees")
    @Test
    void testGetValueSourceDirty() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.prepareParameters();
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef-dirty")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef-dirty")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef-dirty")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("true")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(dateFormat.parse(valueSource.getValue("TIMESTAMP").toString()), is(greaterThanOrEqualTo(timestamp)));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @DisplayName("should handle ignore dirty worktrees if configured")
    @Test
    void testGetValueSourceDisabledDirtyFlag() throws Exception {
        when(repository.isDirty(mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "null";
        mojo.prepareParameters();
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("true")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(dateFormat.parse(valueSource.getValue("TIMESTAMP").toString()), is(greaterThanOrEqualTo(timestamp)));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @DisplayName("should copy the generated file into the target directory")
    @Test
    void testResult() throws Exception {
        mojo.run(repository);

        File targetFile = new File(mojo.outputDirectory, "com/github/koraktor/mavanagaita/GitInfo.java");
        verify(mojo.fileFilter).copyFile(any(File.class), eq(targetFile), eq(true), anyList(), eq("UTF-8"), eq(true));
    }

    @AfterEach
    void teardown() {
        mojo.outputDirectory.delete();
    }
}

