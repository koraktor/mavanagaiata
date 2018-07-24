/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2018, Sebastian Staudt
 *               2015, Kay Hannay
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevFlagSet;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevSort;
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

import static java.util.Collections.min;
import static java.util.Comparator.comparingInt;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

/**
 * Wrapper around JGit's {@link Repository} object to represent a Git
 * repository
 *
 * @author Sebastian Staudt
 */
public class JGitRepository extends AbstractGitRepository {

    private static final int MAX_DESCRIBE_CANDIDATES = 10;

    boolean checked;

    public Repository repository;

    RevCommit headCommit;

    protected ObjectId headObject;

    protected RevWalk revWalk;

    /**
     * Creates a new empty instance
     */
    JGitRepository() {}

    /**
     * Creates a new instance for the given worktree and or Git directory
     *
     * @param workTree The worktree of the repository or {@code null}
     * @param gitDir The GIT_DIR of the repository or {@code null}
     * @throws GitRepositoryException if the parameters do not match a Git
     *         repository
     */
    public JGitRepository(File workTree, File gitDir)
            throws GitRepositoryException {
        buildRepository(workTree, gitDir);
    }

    void buildRepository(File workTree, File gitDir) throws GitRepositoryException {
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

        FileRepositoryBuilder repositoryBuilder = getRepositoryBuilder();
        if (gitDir == null) {
            if (repositoryBuilder.findGitDir(workTree).getGitDir() == null) {
                throw new GitRepositoryException(workTree + " is not inside a Git repository. Please specify the GIT_DIR separately.");
            }

            repositoryBuilder.setWorkTree(repositoryBuilder.getGitDir().getParentFile());
        } else {
            repositoryBuilder.setGitDir(gitDir);
            repositoryBuilder.setWorkTree(workTree);
        }

        try {
            repository = repositoryBuilder.build();
        } catch (IOException e) {
            throw new GitRepositoryException("Could not initialize repository", e);
        }
    }

    @Override
    public void check() throws GitRepositoryException {
        if (!repository.getObjectDatabase().exists()) {
            File path = repository.isBare() ? repository.getDirectory() : repository.getWorkTree();
            throw new GitRepositoryException(path.getAbsolutePath() + " is not a Git repository.");
        }

        checked = true;
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
        Map<String, GitTag> tagCommits = getTags();
        RevCommit start = getHeadRevCommit();

        // Check if the start commit is already tagged
        if (tagCommits.containsKey(start.name())) {
            GitTag tag = getTags().get(start.name());

            return new GitTagDescription(getAbbreviatedCommitId(getHeadCommit()), tag,0);
        }

        try (RevWalk revWalk = getRevWalk()) {
            revWalk.markStart(start);
            revWalk.setRetainBody(false);
            revWalk.sort(RevSort.COMMIT_TIME_DESC);

            final RevFlagSet allFlags = new RevFlagSet();
            final Collection<TagCandidate> candidates = findTagCandidates(revWalk, tagCommits, allFlags);

            if (candidates.isEmpty()) {
                return new GitTagDescription(getAbbreviatedCommitId(getHeadCommit()), null, -1);
            }

            TagCandidate bestCandidate = min(candidates, comparingInt(TagCandidate::getDistance));

            // We hit the maximum of candidates so there may be still be
            // commits that add up to the distance
            if (candidates.size() == MAX_DESCRIBE_CANDIDATES) {
                correctDistance(revWalk, bestCandidate, allFlags);
            }

            return new GitTagDescription(getAbbreviatedCommitId(getHeadCommit()), bestCandidate.tag, bestCandidate.getDistance());
        } catch (IOException e) {
            throw new GitRepositoryException("Could not describe current commit.", e);
        }
    }

    @Override
    public String getHeadRef() {
        return headRef;
    }

    /**
     * Find up to 10 tag candidates in the current branch. One of these should
     * be the latest tag.
     *
     * @param revWalk Repository information
     * @param tagCommits Map of commits that are associated with a tag
     * @param allFlags All flags that have been set so far
     * @return A collection of tag candidates
     * @throws IOException if there’s an error during the rev walk
     */
    private Collection<TagCandidate> findTagCandidates(RevWalk revWalk,
            Map<String, GitTag> tagCommits, RevFlagSet allFlags)
                    throws IOException {
        final Collection<TagCandidate> candidates = new ArrayList<>();
        int distance = 1;
        revWalk.next();
        RevCommit commit;
        while ((commit = revWalk.next()) != null) {
            for (TagCandidate candidate : candidates) {
                candidate.incrementDistanceIfExcludes(commit);
            }

            if (!commit.hasAny(allFlags) && tagCommits.containsKey(commit.name())) {
                GitTag tag = tagCommits.get(commit.name());
                RevFlag flag = revWalk.newFlag(tag.getName());
                candidates.add(new TagCandidate(tag, distance, flag));
                commit.add(flag);
                commit.carry(flag);
                revWalk.carry(flag);
                allFlags.add(flag);
            }

            // Only consider a maximum of 10 candidates
            if (candidates.size() == MAX_DESCRIBE_CANDIDATES) {
                break;
            }

            distance ++;
        }

        return candidates;
    }

    /**
     * Correct the distance for all tag candidates. We have to check all
     * branches to get the correct distance at the end.
     *
     * @param revWalk Repository information
     * @param candidate Collection of tag candidates
     * @param allFlags All flags that have been set so far
     * @throws IOException if there’s an error during the rev walk
     */
    private void correctDistance(RevWalk revWalk, TagCandidate candidate, RevFlagSet allFlags)
            throws IOException {
        RevCommit commit;
        while ((commit = revWalk.next()) != null) {
            if (commit.hasAll(allFlags)) {
                // The commit has all flags already, so we just mark the parents as seen.
                for (RevCommit parent : commit.getParents()) {
                    parent.add(RevFlag.SEEN);
                }
            } else {
                candidate.incrementDistanceIfExcludes(commit);
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
                String.format("Commit \"%s\" could not be abbreviated.", commit.getId()),
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
        return new JGitCommit(getHeadRevCommit());
    }

    /**
     * Creates a new JGit {@code IndexDiff} instance for this repository and
     * worktree
     *
     * @return A new index diff
     * @throws GitRepositoryException if the {@code HEAD} object cannot be
     *         resolved
     * @throws IOException if the index diff cannot be created
     */
    IndexDiff createIndexDiff() throws GitRepositoryException, IOException {
        FileTreeIterator workTreeIterator = new FileTreeIterator(repository);
        return new IndexDiff(repository, getHeadObject(), workTreeIterator);
    }

    /**
     * Runs a diff operation for the repository and worktree
     *
     * @return A index diff for the for the current worktree state
     * @throws GitRepositoryException if the index diff cannot be created
     */
    IndexDiff getIndexDiff() throws GitRepositoryException {
        try {
            IndexDiff indexDiff = createIndexDiff();
            indexDiff.diff();

            return indexDiff;
        } catch (IOException e) {
            throw new GitRepositoryException("Could not create repository diff.", e);
        }
    }

    /**
     * Creates a new JGit {@code FileRepositoryBuilder} instance
     *
     * @return A new repository builder
     */
    FileRepositoryBuilder getRepositoryBuilder() {
        return new FileRepositoryBuilder().readEnvironment();
    }

    @Override
    public Map<String, GitTag> getTags()
            throws GitRepositoryException {
        Map<String, GitTag> tags = new HashMap<>();

        try (RevWalk revWalk = getRevWalk()){
            for (Ref tag : repository.getRefDatabase().getRefsByPrefix(R_TAGS)) {
                try {
                    RevTag revTag = revWalk.lookupTag(tag.getObjectId());
                    RevObject object = revWalk.peel(revTag);
                    if (!(object instanceof RevCommit)) {
                        continue;
                    }
                    tags.put(object.getName(), new JGitTag(revTag));
                } catch (MissingObjectException ignored) {}
            }
        } catch (IOException e) {
            throw new GitRepositoryException("The tags could not be resolved.", e);
        }

        return tags;
    }

    public File getWorkTree() {
        return this.repository.getWorkTree();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean isDirty(boolean ignoreUntracked) throws GitRepositoryException {
        IndexDiff indexDiff = getIndexDiff();

        return !ignoreUntracked && !indexDiff.getUntracked().isEmpty() ||
                !(indexDiff.getAdded().isEmpty() && indexDiff.getChanged().isEmpty() &&
                indexDiff.getRemoved().isEmpty() &&
                indexDiff.getMissing().isEmpty() &&
                indexDiff.getModified().isEmpty() &&
                indexDiff.getConflicting().isEmpty());
    }

    @Override
    public boolean isOnUnbornBranch() throws GitRepositoryException {
        return getHeadObject().equals(ObjectId.zeroId());
    }

    @Override
    public <T extends CommitWalkAction> T walkCommits(T action)
            throws GitRepositoryException {
        action.setRepository(this);
        action.prepare();

        try (RevWalk revWalk = getRevWalk()) {
            revWalk.markStart(getHeadRevCommit());

            RevCommit commit;
            while ((commit = revWalk.next()) != null) {
                action.execute(new JGitCommit(commit));
            }

            return action;
        } catch (IOException e) {
            throw new GitRepositoryException("Could not walk commits.", e);
        }
    }

    /**
     * Returns a commit object for {@code HEAD}
     *
     * @return The commit object for {@code HEAD}
     * @see RevCommit
     * @throws GitRepositoryException if the commit object cannot be retrieved
     */
    RevCommit getHeadRevCommit() throws GitRepositoryException {
        if (headCommit != null) {
            return headCommit;
        }

        try {
            return headCommit = repository.parseCommit(getHeadObject());
        } catch (IOException e) {
            throw new GitRepositoryException(
                    String.format("Commit \"%s\" could not be loaded.",
                        getHeadObject().getName()), e);
        }
    }

    /**
     * Returns the object for the Git ref currently set as {@code HEAD}
     *
     * @return The currently selected {@code HEAD} object
     * @throws GitRepositoryException if the ref cannot be resolved
     */
    protected ObjectId getHeadObject() throws GitRepositoryException {
        if (headObject == null) {
            try {
                Ref head = repository.findRef(headRef);
                if (head == null) {
                    throw new GitRepositoryException(
                            String.format("Ref \"%s\" is invalid.", headRef));
                }

                headObject = head.getObjectId();
            } catch (IOException e) {
                throw new GitRepositoryException(
                    String.format("Ref \"%s\" could not be resolved.", headRef),
                    e);
            }
        }

        if (headObject == null) {
            headObject = ObjectId.zeroId();
        }

        return headObject;
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
        } else {
            revWalk.reset();
        }

        return this.revWalk;
    }

    /**
     * This class represents a tag candidate which could be the latest tag in the branch.
     */
    private class TagCandidate {
        private final GitTag tag;
        private final RevFlag flag;
        private int distance;

        TagCandidate(GitTag tag, int distance, RevFlag flag) {
            this.tag = tag;
            this.distance = distance;
            this.flag = flag;
        }

        int getDistance() {
            return distance;
        }

        void incrementDistanceIfExcludes(RevCommit commit) {
            if (!commit.has(flag)) {
                distance++;
            }
        }
    }

}
