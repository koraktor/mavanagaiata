/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.mockito.stubbing.Answer;

import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitTag;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChangelogMojoTest extends GitOutputMojoAbstractTest<ChangelogMojo> {

    private List<GitCommit> mockCommits;

    private static GitCommit mockCommit(String id, String message) {
        GitCommit commit = mock(GitCommit.class);
        when(commit.getId()).thenReturn(id);
        when(commit.getMessage()).thenReturn(message);
        when(commit.getMessageSubject()).thenReturn(message.split("\\n\\n")[0]);
        return commit;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();

        Locale.setDefault(Locale.ENGLISH);

        mojo.format = new ChangelogFormat();
        mojo.formatTemplate = ChangelogFormat.Formats.DEFAULT;
        mojo.skipMergeCommits = false;
        mojo.skipTagged = false;
        mojo.skipCommitsMatching = null;

        mockCommits = new ArrayList<>();
        mockCommits.add(mockCommit("598a75596868dec45f8e6a808a07d533bc0184f0", "8th commit"));
        mockCommits.add(mockCommit("6727d8c671ce9022047940ebb6568096b4362f90", "7th commit"));
        mockCommits.add(mockCommit("06cee865ab7f006a58be39f1d46f01dcb1880105", "6th commit"));
        mockCommits.add(mockCommit("99982df241980fdeebbb01216b5e80286163c62e", "5th commit"));
        mockCommits.add(mockCommit("afb48c6be4278ba7f5e4197b80adbbb80c6df3a7", "4th commit\n\n[ci skip]"));
        mockCommits.add(mockCommit("5979a86e9bb091fc792529bee68ed222000ebc7e", "3rd commit"));
        mockCommits.add(mockCommit("b3b28176c1a05b76fb9231abe2f2cbbf15a86118", "2nd commit"));
        mockCommits.add(mockCommit("e82314841e1d990eeb33878cae55dadc8a11bf68", "1st commit"));

        HashMap<String, GitTag> tags = new HashMap<>();
        GitTag tag1 = mock(GitTag.class);
        when(tag1.getDate()).thenReturn(new Date(1162580880000L));
        when(tag1.getName()).thenReturn("1.0.0");
        when(tag1.getTimeZone()).thenReturn(TimeZone.getTimeZone("GMT"));
        tags.put("5979a86e9bb091fc792529bee68ed222000ebc7e", tag1);
        GitTag tag2 = mock(GitTag.class);
        when(tag2.getDate()).thenReturn(new Date(1275131880000L));
        when(tag2.getName()).thenReturn("2.0.0");
        when(tag2.getTimeZone()).thenReturn(TimeZone.getTimeZone("GMT+2"));
        tags.put("06cee865ab7f006a58be39f1d46f01dcb1880105", tag2);

        when(repository.getBranch()).thenReturn("master");
        when(repository.getTags()).thenReturn(tags);
        doAnswer((Answer<ChangelogMojo.ChangelogWalkAction>) invocation -> {
            ChangelogMojo.ChangelogWalkAction walkAction = ((ChangelogMojo.ChangelogWalkAction) invocation.getArguments()[0]);
            walkAction.setRepository(repository);
            walkAction.prepare();
            for (GitCommit commit : ChangelogMojoTest.this.mockCommits) {
                walkAction.execute(commit);
            }
            return walkAction;
        }).when(repository).walkCommits(any(ChangelogMojo.ChangelogWalkAction.class));
    }

    @Test
    public void testError() {
        mojo.initConfiguration();

        super.testError("Unable to generate changelog from Git");
    }

    @Test
    public void testCreateGitHubLinks() {
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(false));

        mojo.gitHubProject = "";
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(false));

        mojo.gitHubProject = "mavanagaiata";
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(false));

        mojo.gitHubUser = "";
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(false));

        mojo.gitHubUser = "koraktor";
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(false));

        mojo.format.createLinks = true;
        mojo.initConfiguration();
        assertThat(mojo.format.createLinks, is(true));
    }

    @Test
    public void testCustomization() throws Exception {
        ChangelogDefaultFormat format = new ChangelogDefaultFormat();
        format.branch = "Branch \"%s\"\\n";
        format.branchLink = "\\nGit history for \"%s\" since %s: %s";
        format.commitPrefix = "- ";
        format.createLinks = true;
        format.header = "History\\n-------\\n";
        format.tag = "\\nTag %s on %s\\n";
        format.tagLink = "\\nGit history for %s: %s";

        mojo.dateFormat = "dd.MM.yyyy";
        mojo.footer = "\\nFooter";
        mojo.format = format;
        mojo.gitHubProject = "mavanagaiata";
        mojo.gitHubUser = "koraktor";
        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("History");
        assertOutputLine("-------");
        assertOutputLine("");
        assertOutputLine("Branch \"master\"");
        assertOutputLine("");
        assertOutputLine("- 8th commit");
        assertOutputLine("- 7th commit");
        assertOutputLine("");
        assertOutputLine("Git history for \"master\" since 2.0.0: https://github.com/koraktor/mavanagaiata/compare/2.0.0...master");
        assertOutputLine("");
        assertOutputLine("Tag 2.0.0 on 29.05.2010");
        assertOutputLine("");
        assertOutputLine("- 6th commit");
        assertOutputLine("- 5th commit");
        assertOutputLine("- 4th commit");
        assertOutputLine("");
        assertOutputLine("Git history for 2.0.0: https://github.com/koraktor/mavanagaiata/compare/1.0.0...2.0.0");
        assertOutputLine("");
        assertOutputLine("Tag 1.0.0 on 03.11.2006");
        assertOutputLine("");
        assertOutputLine("- 3rd commit");
        assertOutputLine("- 2nd commit");
        assertOutputLine("- 1st commit");
        assertOutputLine("");
        assertOutputLine("Git history for 1.0.0: https://github.com/koraktor/mavanagaiata/commits/1.0.0");
        assertOutputLine("");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

    @Test
    public void testInitConfiguration() {
        mojo.initConfiguration();

        assertThat(mojo.format, is(instanceOf(ChangelogFormat.class)));
    }

    @Test
    public void testInitConfigurationMarkdownFormat() {
        mojo.formatTemplate = ChangelogFormat.Formats.MARKDOWN;

        mojo.initConfiguration();

        ChangelogMarkdownFormat markdownFormat = new ChangelogMarkdownFormat();
        assertThat(mojo.format.branch, is(equalTo(markdownFormat.branch)));
        assertThat(mojo.format.branchLink, is(equalTo(markdownFormat.branchLink)));
        assertThat(mojo.format.branchOnlyLink, is(equalTo(markdownFormat.branchOnlyLink)));
        assertThat(mojo.format.commitPrefix, is(equalTo(markdownFormat.commitPrefix)));
        assertThat(mojo.format.header, is(equalTo(markdownFormat.header)));
        assertThat(mojo.format.tag, is(equalTo(markdownFormat.tag)));
        assertThat(mojo.format.tagLink, is(equalTo(markdownFormat.tagLink)));
    }

    @Test
    public void testResult() throws Exception {
        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("Changelog");
        assertOutputLine("=========");
        assertOutputLine("");
        assertOutputLine("Commits on branch \"master\"");
        assertOutputLine("");
        assertOutputLine(" * 8th commit");
        assertOutputLine(" * 7th commit");
        assertOutputLine("");
        assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        assertOutputLine("");
        assertOutputLine(" * 6th commit");
        assertOutputLine(" * 5th commit");
        assertOutputLine(" * 4th commit");
        assertOutputLine("");
        assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        assertOutputLine("");
        assertOutputLine(" * 3rd commit");
        assertOutputLine(" * 2nd commit");
        assertOutputLine(" * 1st commit");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

    @Test
    public void testSkipCommits() throws Exception {
        mojo.skipCommitsMatching = "\\[ci skip\\]";
        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("Changelog");
        assertOutputLine("=========");
        assertOutputLine("");
        assertOutputLine("Commits on branch \"master\"");
        assertOutputLine("");
        assertOutputLine(" * 8th commit");
        assertOutputLine(" * 7th commit");
        assertOutputLine("");
        assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        assertOutputLine("");
        assertOutputLine(" * 6th commit");
        assertOutputLine(" * 5th commit");
        assertOutputLine("");
        assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        assertOutputLine("");
        assertOutputLine(" * 3rd commit");
        assertOutputLine(" * 2nd commit");
        assertOutputLine(" * 1st commit");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

    @Test
    public void testStartTagged() throws Exception {
        mockCommits = mockCommits.subList(2, mockCommits.size());

        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("Changelog");
        assertOutputLine("=========");
        assertOutputLine("");
        assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        assertOutputLine("");
        assertOutputLine(" * 6th commit");
        assertOutputLine(" * 5th commit");
        assertOutputLine(" * 4th commit");
        assertOutputLine("");
        assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        assertOutputLine("");
        assertOutputLine(" * 3rd commit");
        assertOutputLine(" * 2nd commit");
        assertOutputLine(" * 1st commit");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

    @Test
    public void testSkipTagged() throws Exception {
        mojo.skipTagged = true;
        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("Changelog");
        assertOutputLine("=========");
        assertOutputLine("");
        assertOutputLine("Commits on branch \"master\"");
        assertOutputLine("");
        assertOutputLine(" * 8th commit");
        assertOutputLine(" * 7th commit");
        assertOutputLine("");
        assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        assertOutputLine("");
        assertOutputLine(" * 5th commit");
        assertOutputLine(" * 4th commit");
        assertOutputLine("");
        assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        assertOutputLine("");
        assertOutputLine(" * 2nd commit");
        assertOutputLine(" * 1st commit");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

    @Test
    public void testUntaggedProject() throws Exception {
        when(repository.getTags())
            .thenReturn(new HashMap<>());

        mojo.initConfiguration();
        mojo.generateOutput(repository, printStream);

        assertOutputLine("Changelog");
        assertOutputLine("=========");
        assertOutputLine("");
        assertOutputLine("Commits on branch \"master\"");
        assertOutputLine("");
        assertOutputLine(" * 8th commit");
        assertOutputLine(" * 7th commit");
        assertOutputLine(" * 6th commit");
        assertOutputLine(" * 5th commit");
        assertOutputLine(" * 4th commit");
        assertOutputLine(" * 3rd commit");
        assertOutputLine(" * 2nd commit");
        assertOutputLine(" * 1st commit");
        assertOutputLine("Footer");
        assertOutputLine(null);
    }

}
