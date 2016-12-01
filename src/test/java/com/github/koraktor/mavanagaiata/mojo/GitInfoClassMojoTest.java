/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2016, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.shared.filtering.MavenFileFilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.koraktor.mavanagaiata.git.GitTagDescription;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.util.FileUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * @author Sebastian Staudt
 */
@PrepareForTest(GitInfoClassMojo.class)
@RunWith(PowerMockRunner.class)
public class GitInfoClassMojoTest extends MojoAbstractTest<GitInfoClassMojo> {

    private Date timestamp;

    @Before
    public void setup() throws Exception{
        super.setup();

        this.mojo.className       = "GitInfo";
        this.mojo.encoding        = "UTF-8";
        this.mojo.fileFilter      = mock(MavenFileFilter.class);
        this.mojo.packageName     = "com.github.koraktor.mavanagaita";
        this.mojo.outputDirectory = File.createTempFile("mavanagaiata-tests", null);
        this.mojo.outputDirectory.delete();
        this.mojo.outputDirectory.mkdirs();
        FileUtils.forceDeleteOnExit(this.mojo.outputDirectory);

        when(this.mojo.project.getVersion()).thenReturn("1.2.3");

        when(this.repository.getAbbreviatedCommitId()).thenReturn("deadbeef");
        GitTagDescription description = mock(GitTagDescription.class);
        when(description.getNextTagName()).thenReturn("v1.2.3");
        when(description.toString()).thenReturn("v1.2.3-4-gdeadbeef");
        this.timestamp = new Date(1275131880000L);
        whenNew(Date.class).withNoArguments().thenReturn(this.timestamp);
        when(this.repository.describe()).thenReturn(description);
        when(this.repository.getBranch()).thenReturn("master");
        when(this.repository.getHeadCommit().getId()).thenReturn("deadbeefdeadbeefdeadbeefdeadbeef");
    }

    @Test
    public void testGetValueSource() throws Exception {
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(this.mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(this.mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("false")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(valueSource.getValue("TIMESTAMP").toString(), is(equalTo(dateFormat.format(this.timestamp))));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @Test
    public void testGetValueSourceDirty() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.prepareParameters();
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(this.mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(this.mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef-dirty")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef-dirty")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef-dirty")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("true")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(valueSource.getValue("TIMESTAMP").toString(), is(equalTo(dateFormat.format(this.timestamp))));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @Test
    public void testGetValueSourceDisabledDirtyFlag() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        mojo.dirtyFlag = "null";
        mojo.prepareParameters();
        MapBasedValueSource valueSource = mojo.getValueSource(repository);

        SimpleDateFormat dateFormat = new SimpleDateFormat(this.mojo.dateFormat);

        assertThat(valueSource.getValue("BRANCH").toString(), is(equalTo("master")));
        assertThat(valueSource.getValue("CLASS_NAME").toString(), is(equalTo(this.mojo.className)));
        assertThat(valueSource.getValue("COMMIT_ABBREV").toString(), is(equalTo("deadbeef")));
        assertThat(valueSource.getValue("COMMIT_SHA").toString(), is(equalTo("deadbeefdeadbeefdeadbeefdeadbeef")));
        assertThat(valueSource.getValue("DESCRIBE").toString(), is(equalTo("v1.2.3-4-gdeadbeef")));
        assertThat(valueSource.getValue("DIRTY").toString(), is(equalTo("true")));
        assertThat(valueSource.getValue("PACKAGE_NAME").toString(), is(equalTo("com.github.koraktor.mavanagaita")));
        assertThat(valueSource.getValue("TAG_NAME").toString(), is(equalTo("v1.2.3")));
        assertThat(valueSource.getValue("TIMESTAMP").toString(), is(equalTo(dateFormat.format(this.timestamp))));
        assertThat(valueSource.getValue("VERSION").toString(), is(equalTo("1.2.3")));
    }

    @Test
    public void testResult() throws Exception {
        mojo.run(repository);

        File targetFile = new File(this.mojo.outputDirectory, "com/github/koraktor/mavanagaita/GitInfo.java");
        verify(this.mojo.fileFilter).copyFile(any(File.class), eq(targetFile), eq(true), anyListOf(FileUtils.FilterWrapper.class), eq("UTF-8"), eq(true));
    }

}
