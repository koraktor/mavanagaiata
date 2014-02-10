/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2014, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitTag;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitChangelogMojoTest extends GitOutputMojoAbstractTest<GitChangelogMojo> {

    protected List<GitCommit> mockCommits;

    private static GitCommit mockCommit(String id, String subject) {
        GitCommit commit = mock(GitCommit.class);
        when(commit.getId()).thenReturn(id);
        when(commit.getMessageSubject()).thenReturn(subject);
        return commit;
    }

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();

        this.mojo.branchFormat = "Commits on branch \"master\"\n";
        this.mojo.commitPrefix = " * ";
        this.mojo.gitHubBranchLinkFormat = "\nSee Git history for changes in the \"%s\" branch since version %s at: %s";
        this.mojo.gitHubBranchOnlyLinkFormat = "\nSee Git history for changes in the \"%s\" branch at: %s";
        this.mojo.gitHubTagLinkFormat = "\nSee Git history for version %s at: %s";
        this.mojo.header       = "Changelog\n=========\n";
        this.mojo.skipTagged   = false;
        this.mojo.tagFormat    = "\nVersion %s – %s\n";

        this.mockCommits = new ArrayList<GitCommit>();
        this.mockCommits.add(mockCommit("598a75596868dec45f8e6a808a07d533bc0184f0", "8th commit"));
        this.mockCommits.add(mockCommit("6727d8c671ce9022047940ebb6568096b4362f90", "7th commit"));
        this.mockCommits.add(mockCommit("06cee865ab7f006a58be39f1d46f01dcb1880105", "6th commit"));
        this.mockCommits.add(mockCommit("99982df241980fdeebbb01216b5e80286163c62e", "5th commit"));
        this.mockCommits.add(mockCommit("afb48c6be4278ba7f5e4197b80adbbb80c6df3a7", "4th commit"));
        this.mockCommits.add(mockCommit("5979a86e9bb091fc792529bee68ed222000ebc7e", "3rd commit"));
        this.mockCommits.add(mockCommit("b3b28176c1a05b76fb9231abe2f2cbbf15a86118", "2nd commit"));
        this.mockCommits.add(mockCommit("e82314841e1d990eeb33878cae55dadc8a11bf68", "1st commit"));

        HashMap<String, GitTag> tags = new HashMap<String, GitTag>();
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

        when(this.repository.getBranch()).thenReturn("master");
        when(this.repository.getTags()).thenReturn(tags);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                GitChangelogMojo.ChangelogWalkAction walkAction = ((GitChangelogMojo.ChangelogWalkAction) invocation.getArguments()[0]);
                for (GitCommit commit : GitChangelogMojoTest.this.mockCommits) {
                    walkAction.execute(commit);
                }
                return null;
            }
        }).when(this.repository).walkCommits(any(GitChangelogMojo.ChangelogWalkAction.class));
    }

    @Test
    public void testError() {
        super.testError("Unable to generate changelog from Git");
    }

    @Test
    public void testCreateGitHubLinks() throws Exception {
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(false));

        this.mojo.gitHubProject = "";
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(false));

        this.mojo.gitHubProject = "mavanagaiata";
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(false));

        this.mojo.gitHubUser = "";
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(false));

        this.mojo.gitHubUser = "koraktor";
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(false));

        this.mojo.createGitHubLinks = true;
        this.mojo.initConfiguration();
        assertThat(this.mojo.createGitHubLinks, is(true));
    }

    @Test
    public void testCustomization() throws Exception {
        this.mojo.branchFormat      = "Branch \"%s\"\n";
        this.mojo.commitPrefix      = "- ";
        this.mojo.createGitHubLinks = true;
        this.mojo.dateFormat        = "dd.MM.yyyy";
        this.mojo.footer            = "\nFooter";
        this.mojo.gitHubBranchLinkFormat = "\nGit history for \"%s\" since %s: %s";
        this.mojo.gitHubTagLinkFormat = "\nGit history for %s: %s";
        this.mojo.gitHubProject     = "mavanagaiata";
        this.mojo.gitHubUser        = "koraktor";
        this.mojo.header            = "History\\n-------\\n";
        this.mojo.tagFormat         = "\nTag %s on %s\n";
        this.mojo.initConfiguration();
        this.mojo.run();

        this.assertOutputLine("History");
        this.assertOutputLine("-------");
        this.assertOutputLine("");
        this.assertOutputLine("Branch \"master\"");
        this.assertOutputLine("");
        this.assertOutputLine("- 8th commit");
        this.assertOutputLine("- 7th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Git history for \"master\" since 2.0.0: https://github.com/koraktor/mavanagaiata/compare/2.0.0...master");
        this.assertOutputLine("");
        this.assertOutputLine("Tag 2.0.0 on 29.05.2010");
        this.assertOutputLine("");
        this.assertOutputLine("- 6th commit");
        this.assertOutputLine("- 5th commit");
        this.assertOutputLine("- 4th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Git history for 2.0.0: https://github.com/koraktor/mavanagaiata/compare/1.0.0...2.0.0");
        this.assertOutputLine("");
        this.assertOutputLine("Tag 1.0.0 on 03.11.2006");
        this.assertOutputLine("");
        this.assertOutputLine("- 3rd commit");
        this.assertOutputLine("- 2nd commit");
        this.assertOutputLine("- 1st commit");
        this.assertOutputLine("");
        this.assertOutputLine("Git history for 1.0.0: https://github.com/koraktor/mavanagaiata/commits/1.0.0");
        this.assertOutputLine("");
        this.assertOutputLine("Footer");
        this.assertOutputLine(null);
    }

    @Test
    public void testInitConfiguration() {
        this.mojo.initConfiguration();

        assertThat(this.mojo.branchFormat, is(equalTo("Commits on branch \"master\"\n")));
        assertThat(this.mojo.gitHubBranchLinkFormat, is(equalTo("\nSee Git history for changes in the \"%s\" branch since version %s at: %s")));
        assertThat(this.mojo.gitHubBranchOnlyLinkFormat, is(equalTo("\nSee Git history for changes in the \"%s\" branch at: %s")));
        assertThat(this.mojo.gitHubTagLinkFormat, is(equalTo("\nSee Git history for version %s at: %s")));
        assertThat(this.mojo.header, is(equalTo("Changelog\n=========\n")));
        assertThat(this.mojo.tagFormat, is(equalTo("\nVersion %s – %s\n")));
    }

    @Test
    public void testResult() throws Exception {
        this.mojo.run();

        this.assertOutputLine("Changelog");
        this.assertOutputLine("=========");
        this.assertOutputLine("");
        this.assertOutputLine("Commits on branch \"master\"");
        this.assertOutputLine("");
        this.assertOutputLine(" * 8th commit");
        this.assertOutputLine(" * 7th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        this.assertOutputLine("");
        this.assertOutputLine(" * 6th commit");
        this.assertOutputLine(" * 5th commit");
        this.assertOutputLine(" * 4th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        this.assertOutputLine("");
        this.assertOutputLine(" * 3rd commit");
        this.assertOutputLine(" * 2nd commit");
        this.assertOutputLine(" * 1st commit");
        this.assertOutputLine("Footer");
        this.assertOutputLine(null);
    }

    @Test
    public void testStartTagged() throws Exception {
        this.mockCommits = this.mockCommits.subList(2, this.mockCommits.size());

        this.mojo.initConfiguration();
        this.mojo.run();

        this.assertOutputLine("Changelog");
        this.assertOutputLine("=========");
        this.assertOutputLine("");
        this.assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        this.assertOutputLine("");
        this.assertOutputLine(" * 6th commit");
        this.assertOutputLine(" * 5th commit");
        this.assertOutputLine(" * 4th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        this.assertOutputLine("");
        this.assertOutputLine(" * 3rd commit");
        this.assertOutputLine(" * 2nd commit");
        this.assertOutputLine(" * 1st commit");
        this.assertOutputLine("Footer");
        this.assertOutputLine(null);
    }

    @Test
    public void testSkipTagged() throws Exception {
        this.mojo.skipTagged = true;
        this.mojo.run();

        this.assertOutputLine("Changelog");
        this.assertOutputLine("=========");
        this.assertOutputLine("");
        this.assertOutputLine("Commits on branch \"master\"");
        this.assertOutputLine("");
        this.assertOutputLine(" * 8th commit");
        this.assertOutputLine(" * 7th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Version 2.0.0 – 05/29/2010 01:18 PM +0200");
        this.assertOutputLine("");
        this.assertOutputLine(" * 5th commit");
        this.assertOutputLine(" * 4th commit");
        this.assertOutputLine("");
        this.assertOutputLine("Version 1.0.0 – 11/03/2006 07:08 PM +0000");
        this.assertOutputLine("");
        this.assertOutputLine(" * 2nd commit");
        this.assertOutputLine(" * 1st commit");
        this.assertOutputLine("Footer");
        this.assertOutputLine(null);
    }

    @Test
    public void testUntaggedProject() throws Exception {
        when(this.repository.getTags())
            .thenReturn(new HashMap<String, GitTag>());

        this.mojo.run();

        this.assertOutputLine("Changelog");
        this.assertOutputLine("=========");
        this.assertOutputLine("");
        this.assertOutputLine("Commits on branch \"master\"");
        this.assertOutputLine("");
        this.assertOutputLine(" * 8th commit");
        this.assertOutputLine(" * 7th commit");
        this.assertOutputLine(" * 6th commit");
        this.assertOutputLine(" * 5th commit");
        this.assertOutputLine(" * 4th commit");
        this.assertOutputLine(" * 3rd commit");
        this.assertOutputLine(" * 2nd commit");
        this.assertOutputLine(" * 1st commit");
        this.assertOutputLine("Footer");
        this.assertOutputLine(null);
    }

}
