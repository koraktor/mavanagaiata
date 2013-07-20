/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.koraktor.mavanagaiata.git.GitCommit;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class GitCommitMojoTest extends MojoAbstractTest<GitCommitMojo> {

    @Before
    public void setup() throws Exception{
        super.setup();

        GitCommit commit = mock(GitCommit.class);
        when(commit.getAuthorDate()).thenReturn(new Date(1162580880000L));
        when(commit.getAuthorEmailAddress()).thenReturn("john.doe@example.com");
        when(commit.getAuthorName()).thenReturn("John Doe");
        when(commit.getAuthorTimeZone()).thenReturn(TimeZone.getTimeZone("GMT"));
        when(commit.getCommitterDate()).thenReturn(new Date(1275131880000L));
        when(commit.getCommitterEmailAddress()).thenReturn("koraktor@gmail.com");
        when(commit.getCommitterName()).thenReturn("Sebastian Staudt");
        when(commit.getCommitterTimeZone()).thenReturn(TimeZone.getTimeZone("GMT+2"));
        when(commit.getId()).thenReturn("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef");

        when(this.repository.getAbbreviatedCommitId()).thenReturn("deadbeef");
        when(this.repository.getHeadCommit()).thenReturn(commit);
    }

    @Test
    public void testError() {
        super.testError("Unable to read Git commit information");
    }

    @Test
    public void testCustomDateFormat() throws MavanagaiataMojoException {
        this.mojo.dateFormat = "dd.MM.yyyy";
        this.mojo.run();

        this.assertProperty("03.11.2006", "commit.author.date");
        this.assertProperty("29.05.2010", "commit.committer.date");
    }

    @Test
    public void testCustomDirtyFlag() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.dirtyFlag = "*";
        this.mojo.run();

        String headId = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef*";

        this.assertProperty("deadbeef*", "commit.abbrev");
        this.assertProperty(headId, "commit.id");
        this.assertProperty(headId, "commit.sha");
    }

    @Test
    public void testDirtyWorktree() throws Exception {
        when(this.repository.isDirty(this.mojo.dirtyIgnoreUntracked)).thenReturn(true);

        this.mojo.run();

        String headId = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef-dirty";

        this.assertProperty("deadbeef-dirty", "commit.abbrev");
        this.assertProperty(headId, "commit.id");
        this.assertProperty(headId, "commit.sha");
        this.assertProperty("true", "commit.dirty");
    }

    @Test
    public void testResult() throws MavanagaiataMojoException {
        this.mojo.run();

        this.assertProperty("deadbeef", "commit.abbrev");
        this.assertProperty("11/03/2006 07:08 PM +0000", "commit.author.date");
        this.assertProperty("John Doe", "commit.author.name");
        this.assertProperty("john.doe@example.com", "commit.author.email");
        this.assertProperty("05/29/2010 01:18 PM +0200", "commit.committer.date");
        this.assertProperty("Sebastian Staudt", "commit.committer.name");
        this.assertProperty("koraktor@gmail.com", "commit.committer.email");
        this.assertProperty("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef", "commit.id");
        this.assertProperty("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef", "commit.sha");
        this.assertProperty("false", "commit.dirty");
    }

}
