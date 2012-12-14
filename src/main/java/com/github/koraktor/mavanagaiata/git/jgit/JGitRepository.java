/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
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
     * @param repository The repository object to wrap
     */
    public JGitRepository(Repository repository) {
        this.commitCache   = new HashMap<ObjectId, RevCommit>();
        this.repository    = repository;
    }

    public void check() throws GitRepositoryException {
        if (!this.repository.getObjectDatabase().exists()) {
            File path = (this.repository.getDirectory() == null) ?
                this.repository.getDirectory() : this.repository.getWorkTree();
            throw new GitRepositoryException(path + " is not a Git repository");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Closes JGit's repository instance.
     *
     * @see Repository#close
     */
    public void close() {
        if (this.repository != null) {
            this.repository.close();
            this.repository = null;
        }
    }

    public GitTagDescription describe() throws GitRepositoryException {
        HashMap<RevCommit, String> tagCommits = new HashMap<RevCommit, String>();
        for (Map.Entry<String, RevTag> tag : this.getRawTags().entrySet()) {
            tagCommits.put((RevCommit) tag.getValue().getObject(), tag.getValue().getName());
        }

        RevCommit start = this.getCommit(this.getHeadObject());
        RevWalk revWalk = this.getRevWalk();
        RevFlag seenFlag = revWalk.newFlag("SEEN");

        int distance = -1;
        GitTag nextTag = null;
        HashSet<RevCommit> commits = new HashSet<RevCommit>();
        commits.add(start);
        while (!commits.isEmpty()) {
            distance ++;
            HashSet<RevCommit> nextCommits = new HashSet<RevCommit>();

            for (RevCommit currentCommit : commits) {
                try {
                    revWalk.parseHeaders(currentCommit);
                } catch (IOException e) {
                    throw new GitRepositoryException("Unable to parse headers of commit " + currentCommit.getName(), e);
                }

                if (currentCommit.has(seenFlag)) {
                    continue;
                }
                currentCommit.add(seenFlag);

                if (tagCommits.containsKey(currentCommit)) {
                    nextTag = this.getTags().get(currentCommit.getId().getName());
                    break;
                }

                if (currentCommit.getParents() != null) {
                    nextCommits.addAll(Arrays.asList(currentCommit.getParents()));
                }
            }

            commits.clear();
            commits.addAll(nextCommits);
        }

        return new GitTagDescription(this, this.getHeadCommit(), nextTag, distance);
    }

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

    public String getBranch() throws GitRepositoryException {
        try {
            return this.repository.getBranch();
        } catch (IOException e) {
            throw new GitRepositoryException("Current branch could not be read.", e);
        }
    }

    public JGitCommit getHeadCommit() throws GitRepositoryException {
        return new JGitCommit(this.getCommit(this.getHeadObject()));
    }

    public Map<String, GitTag> getTags()
            throws GitRepositoryException {
        Map<String, GitTag> tags = new HashMap<String, GitTag>();

        for (Map.Entry<String, RevTag> tag : this.getRawTags().entrySet()) {
            tags.put(tag.getKey(), new JGitTag(tag.getValue()));
        }

        return tags;
    }

    public boolean isDirty() throws GitRepositoryException {
        try {
            FileTreeIterator workTreeIterator = new FileTreeIterator(this.repository);
            IndexDiff indexDiff = new IndexDiff(this.repository, this.getHeadObject(), workTreeIterator);
            indexDiff.diff();
            return !new Status(indexDiff).isClean();
        } catch (IOException e) {
            throw new GitRepositoryException("Could not create repository diff.", e);
        }
    }

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
                } catch(IncorrectObjectTypeException e) {
                    continue;
                }
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
        this.revWalk.reset();

        return this.revWalk;
    }

}
