/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2013, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

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
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ JGitRepository.class })
public class JGitRepositoryTest {

    @Rule
    protected ExpectedException exception = ExpectedException.none();

    protected Repository repo;

    protected JGitRepository repository;

    @Before
    public void setup() throws Exception {
        this.repo = mock(Repository.class, RETURNS_DEEP_STUBS);

        FileRepositoryBuilder repoBuilder = mock(FileRepositoryBuilder.class);
        whenNew(FileRepositoryBuilder.class).withNoArguments().thenReturn(repoBuilder);
        when(repoBuilder.build()).thenReturn(this.repo);

        File someDir = mock(File.class);
        when(someDir.exists()).thenReturn(true);

        this.repository = new JGitRepository(someDir, someDir);
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
        when(indexDiff.getAdded()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getChanged()).thenReturn(Collections.<String>emptySet());
        when(indexDiff.getRemoved()).thenReturn(Collections.<String>emptySet());
        when(indexDiff.getMissing()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getModified()).thenReturn(Collections.<String>emptySet());
        when(indexDiff.getConflicting()).thenReturn(Collections.<String>emptySet());
        when(indexDiff.getUntracked()).thenReturn(Collections.<String>emptySet());

        assertThat(this.repository.isDirty(false), is(false));

        verify(indexDiff).diff();
    }

    @Test
    public void testCleanIgnoreUntracked() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        when(indexDiff.getAdded()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getChanged()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getRemoved()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getMissing()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getModified()).thenReturn(Collections.<String> emptySet());
        when(indexDiff.getConflicting()).thenReturn(Collections.<String> emptySet());

        assertThat(this.repository.isDirty(true), is(false));

        verify(indexDiff).diff();
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
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);

        JGitRepository repo = spy(this.repository);

        Map<String, RevTag> rawTags = new HashMap<String, RevTag>();
        RevTag rawTag = this.createTag("2.0.0", head.getName());
        rawTags.put(head.getName(), rawTag);
        doReturn(rawTags).when(repo).getRawTags();

        Map<String, GitTag> tags = new HashMap<String, GitTag>();
        tags.put(head.getName(), new JGitTag(rawTag));
        doReturn(tags).when(repo).getTags();

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(head.has(RevFlag.SEEN), is(true));
        assertThat(head_1.has(RevFlag.SEEN), is(false));
        assertThat(head_2.has(RevFlag.SEEN), is(false));
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
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);

        JGitRepository repo = spy(this.repository);

        Map<String, RevTag> rawTags = new HashMap<String, RevTag>();
        RevTag rawTag = this.createTag("2.0.0", head_2.getName());
        rawTags.put(head_2.getName(), rawTag);
        doReturn(rawTags).when(repo).getRawTags();

        Map<String, GitTag> tags = new HashMap<String, GitTag>();
        tags.put(head_2.getName(), new JGitTag(rawTag));
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.next()).thenReturn(head, head_1, head_2, null);
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("2.0.0")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(head.has(RevFlag.SEEN), is(true));
        assertThat(head_1.has(RevFlag.SEEN), is(true));
        assertThat(head_2.has(RevFlag.SEEN), is(true));
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
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);

        JGitRepository repo = spy(this.repository);

        Map<String, RevTag> rawTags = new HashMap<String, RevTag>();
        RevTag rawTagA1 = this.createTag("a1", head_a1.getName());
        RevTag rawTagB2 = this.createTag("b2", head_b2.getName());
        rawTags.put(head_a1.getName(), rawTagA1);
        rawTags.put(head_b2.getName(), rawTagB2);
        doReturn(rawTags).when(repo).getRawTags();

        Map<String, GitTag> tags = new HashMap<String, GitTag>();
        tags.put(head_a1.getName(), new JGitTag(rawTagA1));
        tags.put(head_b2.getName(), new JGitTag(rawTagB2));
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.next()).thenReturn(head, head_a1, head_b1, head_b2, null);
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("a1")).thenReturn(seenFlag);
        when(repo.revWalk.newFlag("b2")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(head.has(RevFlag.SEEN), is(true));
        assertThat(head_a1.has(RevFlag.SEEN), is(true));
        assertThat(head_b1.has(RevFlag.SEEN), is(true));
        assertThat(head_b2.has(RevFlag.SEEN), is(true));
        assertThat(description.getNextTagName(), is(equalTo("a1")));
        assertThat(description.toString(), is(equalTo("a1-2-g" + abbrevId.name())));
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
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);

        JGitRepository repo = spy(this.repository);

        Map<String, RevTag> rawTags = new HashMap<String, RevTag>();
        RevTag rawTagA2 = this.createTag("a2", head_a2.getName());
        RevTag rawTagB1 = this.createTag("b1", head_b1.getName());
        rawTags.put(head_a2.getName(), rawTagA2);
        rawTags.put(head_b1.getName(), rawTagB1);
        doReturn(rawTags).when(repo).getRawTags();

        Map<String, GitTag> tags = new HashMap<String, GitTag>();
        tags.put(head_a2.getName(), new JGitTag(rawTagA2));
        tags.put(head_b1.getName(), new JGitTag(rawTagB1));
        doReturn(tags).when(repo).getTags();

        repo.revWalk = mock(RevWalk.class);
        when(repo.revWalk.next()).thenReturn(head, head_a1, head_b1, head_a2, head_b2, null);
        RevFlag seenFlag = RevFlag.UNINTERESTING;
        when(repo.revWalk.newFlag("a2")).thenReturn(seenFlag);
        when(repo.revWalk.newFlag("b1")).thenReturn(seenFlag);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = repo.describe();
        assertThat(head.has(RevFlag.SEEN), is(true));
        assertThat(head_a1.has(RevFlag.SEEN), is(true));
        assertThat(head_a2.has(RevFlag.SEEN), is(true));
        assertThat(head_b1.has(RevFlag.SEEN), is(true));
        assertThat(head_b2.has(RevFlag.SEEN), is(true));
        assertThat(description.getNextTagName(), is(equalTo("b1")));
        assertThat(description.toString(), is(equalTo("b1-2-g" + abbrevId.name())));
    }

    @Test
    public void testDescribeUntagged() throws Exception {
        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        RevCommit head_2 = this.createCommit();
        head.getParents()[0] = head_1;
        head_1.getParents()[0] = head_2;
        AbbreviatedObjectId abbrevId = head.abbreviate(7);
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);

        this.repository.revWalk = mock(RevWalk.class);
        when(this.repository.revWalk.next()).thenReturn(head, head_1, head_2, null);

        when(this.repo.getObjectDatabase().newReader().abbreviate(head)).thenReturn(abbrevId);

        GitTagDescription description = this.repository.describe();
        assertThat(head.has(RevFlag.SEEN), is(true));
        assertThat(head_1.has(RevFlag.SEEN), is(true));
        assertThat(head_2.has(RevFlag.SEEN), is(true));
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
    public void testGetBranch() throws Exception {
        when(this.repo.getBranch()).thenReturn("master");

        assertThat(this.repository.getBranch(), is(equalTo("master")));
    }

    @Test
    public void testGetCommit() throws Exception {
        ObjectId head = mock(ObjectId.class);
        RevWalk revWalk = mock(RevWalk.class);
        RevCommit commit = mock(RevCommit.class);
        whenNew(RevWalk.class).withArguments(this.repo).thenReturn(revWalk);
        when(revWalk.parseCommit(head)).thenReturn(commit);

        assertThat(this.repository.getCommit(head), is(commit));
        assertThat(this.repository.commitCache.get(head), is(commit));
    }

    @Test
    public void testGetCommitCached() throws Exception {
        ObjectId head = mock(ObjectId.class);
        RevCommit commit = mock(RevCommit.class);
        this.repository.commitCache.put(head, commit);

        whenNew(RevWalk.class).withArguments(this.repo).thenReturn(null);

        assertThat(this.repository.getCommit(head), is(commit));

        verifyNew(RevWalk.class, never()).withArguments(this.repo);
    }

    @Test
    public void testGetHeadCommit() throws Exception {
        RevCommit head = this.createCommit();
        this.repository.headObject = mock(ObjectId.class);
        this.repository.commitCache.put(this.repository.headObject, head);
        JGitCommit headCommit = new JGitCommit(head);

        assertThat(this.repository.getHeadCommit(), is(equalTo(headCommit)));
    }

    @Test
    public void testGetHeadObject() throws Exception {
        this.repository.setHeadRef("HEAD");
        ObjectId head = mock(ObjectId.class);
        when(this.repo.resolve("HEAD")).thenReturn(head);

        assertThat(this.repository.getHeadObject(), is(head));
        assertThat(this.repository.headObject, is(head));
    }

    @Test
    public void testGetHeadObjectInvalidHead() throws Exception {
        this.repository.setHeadRef("HEAD");
        when(this.repo.resolve("HEAD")).thenReturn(null);

        try {
            this.repository.getHeadObject();
            fail("No exception thrown");
        } catch (GitRepositoryException e) {
            assertThat(e.getMessage(), is(equalTo("HEAD could not be resolved. You're probably on an unborn branch.")));
        }
    }

    @Test
    public void testGetHeadObjectInvalidRef() throws Exception {
        this.repository.setHeadRef("master");
        when(this.repo.resolve("master")).thenReturn(null);

        try {
            this.repository.getHeadObject();
            fail("No exception thrown");
        } catch (GitRepositoryException e) {
            assertThat(e.getMessage(), is(equalTo("Ref \"master\" is invalid.")));
        }
    }

    @Test
    public void testGetHeadObjectCached() throws Exception {
        this.repository.setHeadRef("HEAD");
        ObjectId head = mock(ObjectId.class);
        this.repository.headObject = head;

        assertThat(this.repository.getHeadObject(), is(this.repository.headObject));

        verify(this.repo, never()).resolve(any(String.class));
    }

    @Test
    public void testGetTags() throws Exception {
        Map<String, RevTag> rawTags = new HashMap<String, RevTag>();
        RevTag rawTag1 = this.createTag();
        rawTags.put("1.0.0", rawTag1);
        RevTag rawTag2 = this.createTag();
        rawTags.put("2.0.0", rawTag2);

        JGitRepository repo = spy(this.repository);
        doReturn(rawTags).when(repo).getRawTags();

        Map<String, GitTag> tags = new HashMap<String, GitTag>();
        JGitTag tag1 = new JGitTag(rawTag1);
        tags.put("1.0.0", tag1);
        JGitTag tag2 = new JGitTag(rawTag2);
        tags.put("2.0.0", tag2);

        assertThat(repo.getTags(), is(equalTo(tags)));
    }

    @Test
    public void testIsDirty() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        HashSet<String> untracked = new HashSet<String>();
        untracked.add("somefile");
        when(indexDiff.getUntracked()).thenReturn(untracked);

        assertThat(this.repository.isDirty(false), is(true));

        verify(indexDiff).diff();
    }

    @Test
    public void testIsDirtyIgnoreUntracked() throws Exception {
        IndexDiff indexDiff = this.mockIndexDiff();
        HashSet<String> added = new HashSet<String>();
        added.add("somefile");
        when(indexDiff.getAdded()).thenReturn(added);

        assertThat(this.repository.isDirty(true), is(true));

        verify(indexDiff).diff();
    }

    @Test
    public void testWalkCommits() throws Exception {
        CommitWalkAction action = mock(CommitWalkAction.class);

        RevWalk revWalk = mock(RevWalk.class);
        whenNew(RevWalk.class).withArguments(this.repo).thenReturn(revWalk);

        RevCommit head = this.createCommit();
        RevCommit head_1 = this.createCommit();
        ObjectId headObjectId = mock(ObjectId.class);
        this.repository.headObject = headObjectId;
        this.repository.commitCache.put(headObjectId, head);

        when(revWalk.next()).thenReturn(head)
            .thenReturn(head_1)
            .thenReturn(null);

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
    public void testGetRawTags() throws Exception {
        RevWalk revWalk = mock(RevWalk.class);
        whenNew(RevWalk.class).withArguments(this.repo).thenReturn(revWalk);

        Map<String, Ref> tagRefs = new HashMap<String, Ref>();
        Ref tagRef1 = mock(Ref.class);
        Ref tagRef2 = mock(Ref.class);
        tagRefs.put("1.0.0", tagRef1);
        tagRefs.put("2.0.0", tagRef2);
        when(this.repo.getTags()).thenReturn(tagRefs);

        RevTag tag1 = this.createTag();
        RevTag tag2 = this.createTag();
        RevCommit commit1 = this.createCommit();
        RevObject commit2 = this.createCommit();
        when(tagRef1.getObjectId()).thenReturn(tag1);
        when(revWalk.parseTag(tag1)).thenReturn(tag1);
        when(revWalk.peel(tag1)).thenReturn(commit1);
        when(tagRef2.getObjectId()).thenReturn(tag2);
        when(revWalk.parseTag(tag2)).thenReturn(tag2);
        when(revWalk.peel(tag2)).thenReturn(commit2);

        Map<String, RevTag> tags = new HashMap<String, RevTag>();
        tags.put(commit1.getName(), tag1);
        tags.put(commit2.getName(), tag2);

        assertThat(this.repository.getRawTags(), is(equalTo(tags)));
    }

    protected IndexDiff mockIndexDiff() throws Exception {
        FileTreeIterator fileTreeIterator = mock(FileTreeIterator.class);
        whenNew(FileTreeIterator.class).
            withArguments(this.repo).thenReturn(fileTreeIterator);

        IndexDiff indexDiff = mock(IndexDiff.class);
        whenNew(IndexDiff.class).
            withParameterTypes(Repository.class, ObjectId.class, WorkingTreeIterator.class).
            withArguments(eq(this.repo), any(ObjectId.class), eq(fileTreeIterator)).
            thenReturn(indexDiff);

        return indexDiff;
    }

    private RevCommit createCommit() {
        return createCommit(1);
    }

    private RevCommit createCommit(int numParents) {
        String parents = "";
        for (; numParents > 0; numParents--) {
            parents += String.format("parent %040x\n", new Random().nextLong());
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

    private RevTag createTag() throws CorruptObjectException {
        return this.createTag("name" + new Random().nextInt(),
                String.format("%040x", new Random().nextLong()));
    }

    private RevTag createTag(String name, String objectId) throws CorruptObjectException {
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
}
