/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.mockito.InOrder;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.eclipse.jgit.lib.Constants.R_TAGS;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JGitRepositoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Repository repo;

    private JGitRepository repository;

    @Before
    public void setup() {
        repository = new JGitRepository();
        repository.setHeadRef("HEAD");
        repository.repository = repo = mock(Repository.class, RETURNS_DEEP_STUBS);
    }

    @Test
    public void createWithWorkTree() throws Exception {
        File workTree = mock(File.class);
        when(workTree.exists()).thenReturn(true);

        File gitDir = mock(File.class);
        when(gitDir.getParentFile()).thenReturn(workTree);

        FileRepositoryBuilder repoBuilder = mock(FileRepositoryBuilder.class);
        when(repoBuilder.findGitDir(any())).thenReturn(repoBuilder);
        when(repoBuilder.getGitDir()).thenReturn(gitDir);

        JGitRepository repository = spy(new JGitRepository());
        when(repository.getRepositoryBuilder()).thenReturn(repoBuilder);

        repository.buildRepository(workTree, null);

        InOrder inOrder = inOrder(repoBuilder);
        inOrder.verify(repoBuilder).findGitDir(workTree);
        inOrder.verify(repoBuilder).setWorkTree(workTree);
    }

    @Test
    public void createWithWorkTreeAndGitDir() throws Exception {
        File workTree = mock(File.class);
        when(workTree.exists()).thenReturn(true);

        File gitDir = mock(File.class);
        when(gitDir.exists()).thenReturn(true);

        FileRepositoryBuilder repoBuilder = mock(FileRepositoryBuilder.class);
        when(repoBuilder.getGitDir()).thenReturn(gitDir);

        JGitRepository repository = spy(new JGitRepository());
        when(repository.getRepositoryBuilder()).thenReturn(repoBuilder);

        repository.buildRepository(workTree, gitDir);

        InOrder inOrder = inOrder(repoBuilder);
        inOrder.verify(repoBuilder).setGitDir(gitDir);
        inOrder.verify(repoBuilder).setWorkTree(workTree);
    }

    @Test
    public void createWithWorkTreeChild() throws Exception {
        File workTree = mock(File.class);

        File workTreeChild = mock(File.class);
        when(workTreeChild.exists()).thenReturn(true);

        FileRepositoryBuilder repoBuilder = mock(FileRepositoryBuilder.class, RETURNS_DEEP_STUBS);
        when(repoBuilder.getGitDir().getParentFile()).thenReturn(workTree);

        JGitRepository repository = spy(new JGitRepository());
        when(repository.getRepositoryBuilder()).thenReturn(repoBuilder);

        repository.buildRepository(workTreeChild, null);

        InOrder inOrder = inOrder(repoBuilder);
        inOrder.verify(repoBuilder).findGitDir(workTreeChild);
        inOrder.verify(repoBuilder).setWorkTree(workTree);
    }

    @Test
    public void testBuildRepositoryFailure() throws Exception {
        repository = spy(repository);
        FileRepositoryBuilder repositoryBuilder = mock(FileRepositoryBuilder.class);
        when(repository.getRepositoryBuilder()).thenReturn(repositoryBuilder);

        Throwable exception = mock(IOException.class);
        when(repositoryBuilder.build()).thenThrow(exception);

        try {
            File gitDir = mock(File.class);
            when(gitDir.exists()).thenReturn(true);
            repository.buildRepository(null, gitDir);
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Could not initialize repository")));
        }
    }

    @Test
    public void testCheckFails() throws GitRepositoryException {
        File gitDir = mock(File.class);
        when(gitDir.getAbsolutePath()).thenReturn("/some/repo/.git");

        when(this.repo.getObjectDatabase().exists()).thenReturn(false);
        when(this.repo.getDirectory()).thenReturn(gitDir);
        when(this.repo.isBare()).thenReturn(true);

        this.exception.expect(GitRepositoryException.class);
        this.exception.expectMessage("/some/repo/.git is not a Git repository.");

        this.repository.check();
    }

    @Test
    public void testCheckFailsWithWorktree() throws GitRepositoryException {
        File workTree = mock(File.class);
        when(workTree.getAbsolutePath()).thenReturn("/some/repo");

        when(this.repo.getObjectDatabase().exists()).thenReturn(false);
        when(this.repo.getWorkTree()).thenReturn(workTree);
        when(this.repo.isBare()).thenReturn(false);

        this.exception.expect(GitRepositoryException.class);
        this.exception.expectMessage("/some/repo is not a Git repository.");

        this.repository.check();
    }

    @Test
    public void testCheckSucceeds() throws GitRepositoryException {
        when(this.repo.getObjectDatabase().exists()).thenReturn(true);

        this.repository.check();
    }

    @Test
    public void testClean() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        when(indexDiff.getAdded()).thenReturn(emptySet());
        when(indexDiff.getChanged()).thenReturn(emptySet());
        when(indexDiff.getRemoved()).thenReturn(emptySet());
        when(indexDiff.getMissing()).thenReturn(emptySet());
        when(indexDiff.getModified()).thenReturn(emptySet());
        when(indexDiff.getConflicting()).thenReturn(emptySet());
        when(indexDiff.getUntracked()).thenReturn(emptySet());

        assertThat(this.repository.isDirty(false), is(false));
    }

    @Test
    public void testCleanIgnoreUntracked() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        when(indexDiff.getAdded()).thenReturn(emptySet());
        when(indexDiff.getChanged()).thenReturn(emptySet());
        when(indexDiff.getRemoved()).thenReturn(emptySet());
        when(indexDiff.getMissing()).thenReturn(emptySet());
        when(indexDiff.getModified()).thenReturn(emptySet());
        when(indexDiff.getConflicting()).thenReturn(emptySet());

        assertThat(this.repository.isDirty(true), is(false));
    }

    @Test
    public void testClose() {
        this.repository.close();

        verify(this.repo).close();
    }

    @Test
    public void testCloseNullRepository() {
        this.repository.repository = null;
        this.repository.close();
    }

    @Test
    public void testDescribeExactTagged() throws Exception {
        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        RevCommit head_2 = this.createCommit();
        head.getParents()[0] = head_1;
        head_1.getParents()[0] = head_2;
        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        JGitRepository repo = spy(this.repository);

        Map<String, JGitTag> tags = new HashMap<>();
        JGitTag tag = createTag("2.0.0", head.getName());
        tags.put(head.getName(), tag);
        doReturn(tags).when(repo).getTags();

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(description.getNextTagName(), is(equalTo("2.0.0")));
        assertThat(description.toString(), is(equalTo("2.0.0")));
    }

    @Test
    public void testDescribeTagged() throws Exception {
        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        RevCommit head_2 = this.createCommit();
        head.getParents()[0] = head_1;
        head_1.getParents()[0] = head_2;
        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        JGitRepository repo = spy(this.repository);

        Map<String, JGitTag> tags = new HashMap<>();
        JGitTag tag = createTag("2.0.0", head.getName());
        tags.put(head_2.getName(), tag);
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.iterator()).
            thenReturn(asList(head, head_1, head_2).iterator());
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("2.0.0")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(description.getNextTagName(), is(equalTo("2.0.0")));
        assertThat(description.toString(), is(equalTo("2.0.0-2-g" + abbrevId.name())));
    }

    @Test
    public void testDescribeTwoTags() throws Exception {
        RevCommit head = this.createCommit(2);
        RevCommit head_a1 = this.createCommit();
        RevCommit head_b1 = this.createCommit();
        RevCommit head_b2 = this.createCommit();

        head.getParents()[0] = head_a1;
        head.getParents()[1] = head_b1;
        head_b1.getParents()[0] = head_b2;

        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        JGitRepository repo = spy(this.repository);

        Map<String, JGitTag> tags = new HashMap<>();
        JGitTag tagA1 = createTag("a1", head.getName());
        JGitTag tagB2 = createTag("b2", head.getName());
        tags.put(head_a1.getName(), tagA1);
        tags.put(head_b2.getName(), tagB2);
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.iterator()).
            thenReturn(asList(head, head_a1, head_b1, head_b2).iterator());
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("a1")).thenReturn(seenFlag);
        when(repo.revWalk.newFlag("b2")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(description.getNextTagName(), is(equalTo("a1")));
        assertThat(description.toString(), is(equalTo("a1-3-g" + abbrevId.name())));
    }

    @Test
    public void testDescribeTwoBranches() throws Exception {
        RevCommit head = this.createCommit(2);
        RevCommit head_a1 = this.createCommit();
        RevCommit head_a2 = this.createCommit();
        RevCommit head_b1 = this.createCommit();
        RevCommit head_b2 = this.createCommit();

        head.getParents()[0] = head_a1;
        head_a1.getParents()[0] = head_a2;
        head.getParents()[1] = head_b1;
        head_b1.getParents()[0] = head_b2;

        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        JGitRepository repo = spy(this.repository);

        Map<String, JGitTag> tags = new HashMap<>();
        JGitTag tagA1 = createTag("a2", head.getName());
        JGitTag tagB2 = createTag("b1", head.getName());
        tags.put(head_a2.getName(), tagA1);
        tags.put(head_b1.getName(), tagB2);
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.iterator()).
            thenReturn(asList(head, head_a1, head_b1, head_a2, head_b2).iterator());
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("a2")).thenReturn(seenFlag);
        when(repo.revWalk.newFlag("b1")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(description.getNextTagName(), is(equalTo("b1")));
        assertThat(description.toString(), is(equalTo("b1-3-g" + abbrevId.name())));
    }

    @Test
    public void testDescribeUntagged() throws Exception {
        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        RevCommit head_2 = this.createCommit();
        head.getParents()[0] = head_1;
        head_1.getParents()[0] = head_2;
        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        repository.revWalk = mock(RevWalk.class);
        when(repository.revWalk.iterator()).
            thenReturn(asList(head, head_1, head_2).iterator());

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = this.repository.describe();
        assertThat(description.getNextTagName(), is(equalTo("")));
        assertThat(description.toString(), is(equalTo(abbrevId.name())));
    }

    @Test
    public void testGetAbbreviatedCommitId() throws Exception {
        RevCommit rawCommit = this.createCommit();
        AbbreviatedObjectId abbrevId = rawCommit.abbreviate(7);
        JGitCommit commit = new JGitCommit(rawCommit);

        when(this.repo.getObjectDatabase().newReader().abbreviate(rawCommit)).thenReturn(abbrevId);

        assertThat(this.repository.getAbbreviatedCommitId(commit), is(equalTo(rawCommit.getName().substring(0, 7))));
    }

    @Test
    public void testGetAbbreviatedCommitIdFailure() throws Exception {
        RevCommit rawCommit = createCommit();
        JGitCommit commit = new JGitCommit(rawCommit);

        Throwable exception = mock(IOException.class);
        when(this.repo.getObjectDatabase().newReader().abbreviate(rawCommit)).thenThrow(exception);

        try {
            repository.getAbbreviatedCommitId(commit);
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Commit \"" + commit.getId() + "\" could not be abbreviated.")));
        }
    }

    @Test
    public void testGetBranch() throws Exception {
        when(this.repo.getBranch()).thenReturn("master");

        assertThat(this.repository.getBranch(), is(equalTo("master")));
    }

    @Test
    public void testGetBranchFailure() throws Exception {
        FileNotFoundException exception = new FileNotFoundException();
        when(repo.getBranch()).thenThrow(exception);

        try {
            repository.getBranch();
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Current branch could not be read.")));
        }
    }

    @Test
    public void testGetHeadRevCommit() throws Exception {
        ObjectId head = mock(ObjectId.class);
        RevCommit commit = mock(RevCommit.class);
        RevWalk revWalk = mockRevWalk();
        when(revWalk.parseCommit(head)).thenReturn(commit);

        assertThat(repository.getHeadRevCommit(), is(commit));
        assertThat(repository.headCommit, is(commit));
    }

    @Test
    public void testGetCommitCached() throws Exception {
        RevCommit commit = mock(RevCommit.class);
        repository.headCommit = commit;

        assertThat(repository.getHeadRevCommit(), is(commit));

        verify(repository.repository, never()).parseCommit(any(ObjectId.class));
    }

    @Test
    public void testGetHeadCommit() throws Exception {
        RevCommit head = this.createCommit();
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;
        JGitCommit headCommit = new JGitCommit(head);

        assertThat(this.repository.getHeadCommit(), is(equalTo(headCommit)));
    }

    @Test
    public void testGetHeadObject() throws Exception {
        ObjectId head = mock(ObjectId.class);
        when(repo.resolve("HEAD")).thenReturn(head);

        assertThat(repository.getHeadObject(), is(head));
        assertThat(repository.headObject, is(head));
    }

    @Test
    public void testGetHeadObjectFailure() throws Exception {
        Throwable exception = mock(IOException.class);

        repository.setHeadRef("broken");
        when(repo.findRef("broken")).thenThrow(exception);

        try {
            repository.getHeadObject();
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Ref \"broken\" could not be resolved.")));
        }
    }

    @Test
    public void testGetHeadObjectInvalidRef() throws Exception {
        when(repo.findRef("HEAD")).thenReturn(null);

        try {
            repository.getHeadObject();
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(nullValue()));
            assertThat(e.getMessage(), is(equalTo("Ref \"HEAD\" is invalid.")));
        }
    }

    @Test
    public void testGetHeadObjectCached() throws Exception {
        this.repository.setHeadRef("HEAD");
        ObjectId head = mock(ObjectId.class);
        this.repository.headObject = head;

        assertThat(this.repository.getHeadObject(), is(head));

        verify(this.repo, never()).resolve(any(String.class));
    }

    @Test
    public void testGetTags() throws Exception {
        RevWalk revWalk = mockRevWalk();

        Ref tagRef1 = mock(Ref.class);
        Ref tagRef2 = mock(Ref.class);
        List<Ref> tagRefs = asList(tagRef1, tagRef2);
        when(repo.getRefDatabase().getRefsByPrefix(R_TAGS)).thenReturn(tagRefs);

        RevTag rawTag1 = createRawTag();
        RevTag rawTag2 = createRawTag();
        RevCommit commit1 = createCommit();
        RevObject commit2 = createCommit();
        when(tagRef1.getObjectId()).thenReturn(rawTag1);
        when(revWalk.lookupTag(rawTag1)).thenReturn(rawTag1);
        when(revWalk.peel(rawTag1)).thenReturn(commit1);
        when(tagRef2.getObjectId()).thenReturn(rawTag2);
        when(revWalk.lookupTag(rawTag2)).thenReturn(rawTag2);
        when(revWalk.peel(rawTag2)).thenReturn(commit2);

        Map<String, GitTag> tags = new HashMap<>();
        JGitTag tag1 = new JGitTag(rawTag1);
        tags.put(commit1.name(), tag1);
        JGitTag tag2 = new JGitTag(rawTag2);
        tags.put(commit2.name(), tag2);

        assertThat(repository.getTags(), is(equalTo(tags)));
    }

    @Test
    public void testIsDirty() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        HashSet<String> untracked = new HashSet<>();
        untracked.add("somefile");
        when(indexDiff.getUntracked()).thenReturn(untracked);

        assertThat(this.repository.isDirty(false), is(true));
    }

    @Test
    public void testIsDirtyFailure() throws Exception {
        Throwable exception = new IOException();
        repository = spy(repository);
        doThrow(exception).when(repository).createIndexDiff();

        try {
            repository.isDirty(false);
            fail("No exception thrown.");
        } catch (GitRepositoryException e) {
            assertThat(e.getCause(), is(exception));
            assertThat(e.getMessage(), is(equalTo("Could not create repository diff.")));
        }
    }

    @Test
    public void testIsDirtyIgnoreUntracked() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        HashSet<String> added = new HashSet<>();
        added.add("somefile");
        when(indexDiff.getAdded()).thenReturn(added);

        assertThat(this.repository.isDirty(true), is(true));
    }

    @Test
    public void testIsOnUnbornBranch() throws Exception {
        when(repo.resolve("HEAD")).thenReturn(ObjectId.zeroId());

        assertThat(repository.isOnUnbornBranch(), is(true));
    }

    @Test
    public void testLoadTag() throws Exception {
        RevTag rawTag = RevTag.parse(("object 4b825dc642cb6eb9a060e54bf8d69288fbee4904\n" +
            "type commit\n" +
            "tag 1.0.0\n" +
            "tagger Sebastian Staudt <koraktor@gmail.com> 1275131880 +0200\n" +
            "\n" +
            "Version 1.0.0\n").getBytes());
        Date tagDate = new Date(1275131880000L);

        repository.revWalk = mock(RevWalk.class);
        JGitTag tag = new JGitTag(rawTag);

        repository.loadTag(tag);

        assertThat(tag.getDate(), is(equalTo(tagDate)));
        assertThat(tag.getTimeZone(), is(equalTo(TimeZone.getTimeZone("GMT+0200"))));

        verify(repository.revWalk).parseBody(rawTag);
    }

    @Test
    public void testWalkCommits() throws Exception {
        CommitWalkAction action = mock(CommitWalkAction.class);
        RevWalk revWalk = mockRevWalk();

        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        repository.headObject = mock(ObjectId.class);
        repository.headCommit = head;

        when(revWalk.iterator()).
            thenReturn(asList(head, head_1).iterator());

        this.repository.walkCommits(action);

        JGitCommit commit1 = new JGitCommit(head);
        JGitCommit commit2 = new JGitCommit(head_1);

        verify(revWalk).markStart(head);
        InOrder inOrder = inOrder(action);
        inOrder.verify(action).execute(commit1);
        inOrder.verify(action).execute(commit2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetWorktree() {
        assertThat(repository.getWorkTree(), is(equalTo(repo.getWorkTree())));
    }

    private IndexDiff mockIndexDiff() throws Exception {
        repository = spy(repository);

        IndexDiff indexDiff = mock(IndexDiff.class);
        doReturn(indexDiff).when(repository).createIndexDiff();

        return indexDiff;
    }

    private RevWalk mockRevWalk() {
        repository = spy(repository);

        RevWalk revWalk = mock(RevWalk.class);
        doReturn(revWalk).when(repository).getRevWalk();

        return revWalk;
    }

    private RevCommit createCommit() {
        return createCommit(1);
    }

    private RevCommit createCommit(int numParents) {
        StringBuilder parents = new StringBuilder();
        for (; numParents > 0; numParents--) {
            parents.append(String.format("parent %040x\n", new java.util.Random().nextLong()));
        }
        String commitData = String.format("tree %040x\n" +
            parents +
            "author Sebastian Staudt <koraktor@gmail.com> %d +0100\n" +
            "committer Sebastian Staudt <koraktor@gmail.com> %d +0100\n\n" +
            "%s",
            new Random().nextLong(),
            new Date().getTime(),
            new Date().getTime(),
            "Commit subject");
        return RevCommit.parse(commitData.getBytes());
    }

    private RevTag createRawTag() throws CorruptObjectException {
        return createRawTag("name" + new Random().nextInt(),
                String.format("%040x", new Random().nextLong()));
    }

    private RevTag createRawTag(String name, String objectId) throws CorruptObjectException {
        String tagData = String.format("object %s\n" +
            "type commit\n" +
            "tag %s\n" +
            "tagger Sebastian Staudt <koraktor@gmail.com> %d +0100\n" +
            "%s",
            objectId,
            name,
            new Date().getTime(),
            "Tag subject");
        return RevTag.parse(tagData.getBytes());
    }

    private JGitTag createTag(String name, String objectId) throws CorruptObjectException {
        return new JGitTag(createRawTag(name, objectId));
    }
}
