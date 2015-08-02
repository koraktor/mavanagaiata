/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2015, Sebastian Staudt
 *               2015, Kay Hannay
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevFlagSet;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;

import com.github.koraktor.mavanagaiata.git.AbstractGitRepository;
import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitCommit;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;
import com.github.koraktor.mavanagaiata.git.GitTagDescription;

/**
 * Wrapper around JGit's {@link Repository} object to represent a Git
 * repository
 *
 * @author Sebastian Staudt
 */
public class JGitRepository extends AbstractGitRepository {

    protected Map<ObjectId, RevCommit> commitCache;

    protected Repository repository;

    protected ObjectId headObject;

    protected RevWalk revWalk;

    /**
     * Creates a new instance from a JGit repository object
     *
     * @param workTree The worktree of the repository or {@code null}
     * @param gitDir The GIT_DIR of the repository or {@code null}
     * @throws GitRepositoryException if the parameters do not match a Git
     *         repository
     */
    public JGitRepository(File workTree, File gitDir)
            throws GitRepositoryException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.readEnvironment();

        if (gitDir == null && workTree == null) {
            throw new GitRepositoryException("Neither worktree nor GIT_DIR is set.");
        } else {
            if (workTree != null && !workTree.exists()) {
                throw new GitRepositoryException("The worktree " + workTree + " does not exist");
            }
            if (gitDir != null && !gitDir.exists()) {
                throw new GitRepositoryException("The GIT_DIR " + gitDir + " does not exist");
            }
        }

        if (gitDir != null) {
            repositoryBuilder.setGitDir(gitDir);
            repositoryBuilder.setWorkTree(workTree);
        } else {
            repositoryBuilder.findGitDir(workTree);

            if (repositoryBuilder.getGitDir() == null) {
                throw new GitRepositoryException(workTree + " is not inside a Git repository. Please specify the GIT_DIR separately.");
            }

            repositoryBuilder.setWorkTree(repositoryBuilder.getGitDir().getParentFile());
        }

        try {
            this.repository = repositoryBuilder.build();
        } catch (IOException e) {
            throw new GitRepositoryException("Could not initialize repository", e);
        }

        this.commitCache = new HashMap<ObjectId, RevCommit>();
    }

    @Override
    public void check() throws GitRepositoryException {
        if (!this.repository.getObjectDatabase().exists()) {
            File path = (this.repository.isBare()) ?
                this.repository.getDirectory() : this.repository.getWorkTree();
            throw new GitRepositoryException(path.getAbsolutePath() + " is not a Git repository.");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Closes JGit's repository instance.
     *
     * @see Repository#close
     */
    @Override
    public void close() {
        if (this.repository != null) {
            this.repository.close();
            this.repository = null;
        }
    }

    @Override
    public GitTagDescription describe() throws GitRepositoryException {
        final Map<RevCommit, RevTag> tagCommits = new HashMap<RevCommit, RevTag>();
        for (RevTag tag : this.getRawTags().values()) {
            tagCommits.put((RevCommit)tag.getObject(), tag);
        }

        final RevFlagSet allFlags = new RevFlagSet();
        final RevCommit start = this.getCommit(this.getHeadObject());
        final RevWalk revWalk = this.getRevWalk();

        try {
            //Check, if the start commit is a tag already
            if (tagCommits.containsKey(start)) {
                GitTag tag = this.getTags().get(start.getId().getName());

                return new GitTagDescription(this, this.getHeadCommit(), tag, 0);
            }

            revWalk.markStart(start);
            final Collection<TagCandidate> candidates = findTagCandidates(revWalk, tagCommits, allFlags);

            if (candidates.isEmpty()) {
                return new GitTagDescription(this, this.getHeadCommit(), null, -1);
            }

            //Now we have to correct the distance of the tag candidates
            correctDistance(revWalk, candidates, allFlags);

            TagCandidate bestCandidate = Collections.min(candidates, new Comparator<TagCandidate>() {
                @Override
                public int compare(TagCandidate tag1, TagCandidate tag2) {
                    return Integer.compare(tag1.distance, tag2.distance);
                }
            });

            GitTag tag = new JGitTag(bestCandidate.commit);

            return new GitTagDescription(this, this.getHeadCommit(), tag, bestCandidate.distance);
        } catch (Exception e) {
            throw new GitRepositoryException("Could not describe current commit.", e);
        } finally {
            revWalk.release();
        }
    }

    /**
     * Find up to 10 tag candidates in the current branch. One of these should be the latest tag.
     *
     * @param revWalk Repository information
     * @param tagCommits Map of commits that are associated with a tag
     * @param allFlags All flags that have been set so far
     *
     * @return A collection of tag candidates
     *
     * @throws IOException
     */
    private Collection<TagCandidate> findTagCandidates(RevWalk revWalk,
            Map<RevCommit,RevTag> tagCommits, RevFlagSet allFlags)
                    throws IOException {
        final Collection<TagCandidate> candidates = new ArrayList<TagCandidate>();
        int distance = 0;
        RevCommit commit;
        while ((commit = revWalk.next()) != null) {
            commit.add(RevFlag.SEEN);
            if (!commit.hasAny(allFlags)) {
                if (tagCommits.containsKey(commit)) {
                    RevTag tagCommit = tagCommits.get(commit);
                    RevFlag flag = revWalk.newFlag(tagCommit.getTagName());
                    candidates.add(new TagCandidate(tagCommit, distance, flag));
                    commit.add(flag);
                    commit.carry(flag);
                    revWalk.carry(flag);
                    allFlags.add(flag);
                }
            }
            for (TagCandidate candidate : candidates) {
                if (!candidate.isRelated(commit)) {
                    candidate.distance++;
                }
            }
            if (candidates.size() >= 10) {
                break;
            }
            distance++;
        }
        return candidates;
    }

    /**
     * Correct the distance for all tag candidates. We have to check all branches to get the correct
     * distance at the end.
     *
     * @param revWalk Repository information
     * @param candidates Collection of tag candidates
     * @param allFlags All flags that have been set so far
     *
     * @throws IOException
     */
    private void correctDistance(RevWalk revWalk, Collection<TagCandidate> candidates, RevFlagSet allFlags)
            throws IOException {
        RevCommit commit;
        while ((commit = revWalk.next()) != null) {
            if (commit.hasAll(allFlags)) {
                // The commit has all flags already, so we just mark the parents as seen.
                for (RevCommit parent : commit.getParents()) {
                    parent.add(RevFlag.SEEN);
                }
            } else {
                for (TagCandidate candidate : candidates) {
                    if (!candidate.isRelated(commit)) {
                        candidate.distance ++;
                    }
                }
            }
        }
    }

    @Override
    public String getAbbreviatedCommitId(GitCommit commit) throws GitRepositoryException {
        try {
            RevCommit rawCommit = ((JGitCommit) commit).commit;
            return this.repository.getObjectDatabase().newReader()
                    .abbreviate(rawCommit).name();
        } catch (IOException e) {
            throw new GitRepositoryException(
                String.format("Commit \"%s\" could not be abbreviated.", this.getHeadObject().getName()),
                e);
        }
    }

    @Override
    public String getBranch() throws GitRepositoryException {
        try {
            return this.repository.getBranch();
        } catch (IOException e) {
            throw new GitRepositoryException("Current branch could not be read.", e);
        }
    }

    @Override
    public JGitCommit getHeadCommit() throws GitRepositoryException {
        return new JGitCommit(this.getCommit(this.getHeadObject()));
    }

    @Override
    public Map<String, GitTag> getTags()
            throws GitRepositoryException {
        Map<String, GitTag> tags = new HashMap<String, GitTag>();

        for (Map.Entry<String, RevTag> tag : this.getRawTags().entrySet()) {
            tags.put(tag.getKey(), new JGitTag(tag.getValue()));
        }

        return tags;
    }

    public File getWorkTree() {
        return this.repository.getWorkTree();
    }

    @Override
    public boolean isDirty(boolean ignoreUntracked) throws GitRepositoryException {
        try {
            FileTreeIterator workTreeIterator = new FileTreeIterator(this.repository);
            IndexDiff indexDiff = new IndexDiff(this.repository, this.getHeadObject(), workTreeIterator);
            indexDiff.diff();

            return !ignoreUntracked && !indexDiff.getUntracked().isEmpty() ||
                    !(indexDiff.getAdded().isEmpty() && indexDiff.getChanged().isEmpty() &&
                    indexDiff.getRemoved().isEmpty() &&
                    indexDiff.getMissing().isEmpty() &&
                    indexDiff.getModified().isEmpty() &&
                    indexDiff.getConflicting().isEmpty());

        } catch (IOException e) {
            throw new GitRepositoryException("Could not create repository diff.", e);
        }
    }

    @Override
    public void walkCommits(CommitWalkAction action)
            throws GitRepositoryException {
        try {
            RevWalk revWalk = this.getRevWalk();
            revWalk.markStart(this.getCommit(this.getHeadObject()));

            RevCommit commit;
            while((commit = revWalk.next()) != null) {
                action.execute(new JGitCommit(commit));
            }
        } catch (IOException e) {
            throw new GitRepositoryException("", e);
        }
    }

    /**
     * Returns a commit object for the given object ID
     *
     * @param id The object ID of the commit
     * @return The commit object for the given object ID
     * @see RevCommit
     * @throws GitRepositoryException if the commit object cannot be retrieved
     */
    protected RevCommit getCommit(ObjectId id) throws GitRepositoryException {
        if (this.commitCache.containsKey(id)) {
            return this.commitCache.get(id);
        }

        try {
            RevWalk revWalk = this.getRevWalk();
            RevCommit commit = revWalk.parseCommit(id);

            this.commitCache.put(id, commit);

            return commit;
        } catch (IncorrectObjectTypeException e) {
            throw new GitRepositoryException(
                    String.format("Object \"%s\" is not a commit.", id.getName()),
                    e);
        } catch (MissingObjectException e) {
            throw new GitRepositoryException(
                    String.format("Commit \"%s\" is missing.", id.getName()),
                    e);
        } catch (IOException e) {
            throw new GitRepositoryException(
                    String.format("Commit \"%s\" could not be loaded.", id.getName()),
                    e);
        }
    }

    /**
     * Returns the object for the Git ref currently set as {@code HEAD}
     *
     * @return The currently selected {@code HEAD} object
     * @throws GitRepositoryException if the ref cannot be resolved
     */
    protected ObjectId getHeadObject() throws GitRepositoryException {
        if (this.headObject == null) {
            try {
                this.headObject = this.repository.resolve(this.headRef);
            } catch (AmbiguousObjectException e) {
                throw new GitRepositoryException(
                    String.format("Ref \"%s\" is ambiguous.", this.headRef),
                    e);
            } catch (IOException e) {
                throw new GitRepositoryException(
                    String.format("Ref \"%s\" could not be resolved.", this.headRef),
                    e);
            }
        }

        if (this.headObject == null) {
            if (this.headRef.equals("HEAD")) {
                throw new GitRepositoryException(
                    "HEAD could not be resolved. You're probably on an unborn branch.");
            }
            throw new GitRepositoryException(
                String.format("Ref \"%s\" is invalid.", this.headRef));
        }

        return this.headObject;
    }

    /**
     * Returns a map of raw JGit tags available in this repository
     * <p>
     * The keys of the map are the SHA IDs of the objects referenced by the
     * tags. The map's values are the raw tags themselves.
     * <p>
     * <em>Note</em>: Only annotated tags referencing commit objects will be
     * returned.
     *
     * @return A map of raw JGit tags in this repository
     * @throws GitRepositoryException if an error occurs while determining the
     *         tags in this repository
     */
    protected Map<String, RevTag> getRawTags()
            throws GitRepositoryException {
        RevWalk revWalk = this.getRevWalk();
        Map<String, Ref> tagRefs = this.repository.getTags();
        Map<String, RevTag> tags = new HashMap<String, RevTag>();

        try {
            for (Map.Entry<String, Ref> tag : tagRefs.entrySet()) {
                try {
                    RevTag revTag = revWalk.parseTag(tag.getValue().getObjectId());
                    RevObject object = revWalk.peel(revTag);
                    if (!(object instanceof RevCommit)) {
                        continue;
                    }
                    tags.put(object.getName(), revTag);
                } catch (IncorrectObjectTypeException ignored) {}
            }
        } catch (MissingObjectException e) {
            throw new GitRepositoryException("The tags could not be resolved.", e);
        } catch (IOException e) {
            throw new GitRepositoryException("The tags could not be resolved.", e);
        }

        return tags;
    }

    /**
     * Gets a JGit {@code RevWalk} instance for this repository
     * <p>
     * Creates a new instance or resets an existing one.
     *
     * @return A {@code RevWalk} instance for this repository
     */
    protected RevWalk getRevWalk() {
        if (this.revWalk == null) {
            this.revWalk = new RevWalk(this.repository);
        }

        return this.revWalk;
    }

    /**
     * This class represents a tag candidate which could be the latest tag in the branch.
     */
    private class TagCandidate {
        private final RevTag commit;
        private int distance;
        private final RevFlag flag;

        TagCandidate(RevTag commit, int distance, RevFlag flag) {
            this.commit = commit;
            this.distance = distance;
            this.flag = flag;
        }

        boolean isRelated(RevCommit commit) {
            return commit.has(flag);
        }
    }

}
