/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2012-2020, Sebastian Staudt
 *               2015, Kay Hannay
 */

package com.github.koraktor.mavanagaiata.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.DescribeCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevWalkException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.FileUtils.*;
import static org.eclipse.jgit.lib.Constants.*;

/**
 * Wrapper around JGit's {@link Repository} object to represent a Git
 * repository
 *
 * @author Sebastian Staudt
 */
public class JGitRepository extends AbstractGitRepository {

    static final String COMMONDIR_FILE = "commondir";
    static final Pattern DESCRIBE_PATTERN = Pattern.compile("(.*)-([1-9][0-9]*)-g([0-9a-f]+)$");
    static final String GITDIR_FILE = "gitdir";
    private static final String INDEX_FILE = "index";
    static final String REF_LINK_PREFIX = "ref: ";

    private boolean checked;
    Repository repository;
    RevCommit headCommit;
    ObjectId headObject;

    /**
     * Creates a new empty instance
     */
    JGitRepository() {}

    /**
     * Creates a new instance for the given worktree and or Git directory
     *
     * @param workTree The worktree of the repository or {@code null}
     * @param gitDir The GIT_DIR of the repository or {@code null}
     * @param headRef The ref to use as {@code HEAD}
     * @throws GitRepositoryException if the parameters do not match a Git
     *         repository
     */
    public JGitRepository(File workTree, File gitDir, String headRef)
            throws GitRepositoryException {
        this.headRef = headRef;

        buildRepository(workTree, gitDir);
    }

    final void buildRepository(File workTree, File gitDir) throws GitRepositoryException {
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
            resolveGitDir(workTree, repositoryBuilder);
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
        if (repository != null) {
            repository.close();
            repository = null;
        }
    }

    @Override
    public GitTagDescription describe() throws GitRepositoryException {
        DescribeCommand command = getDescribeCommand();

        try {
            command.setTarget(getHeadObject());

            String abbrev;
            int distance;
            String tagName;

            String describe = command.call();
            if (describe == null) {
                abbrev = getAbbreviatedCommitId(getHeadCommit());
                distance = -1;
                tagName = null;
            } else {
                Matcher describeMatcher = DESCRIBE_PATTERN.matcher(describe);

                if (describeMatcher.matches()) {
                    abbrev = describeMatcher.group(3);
                    distance = Integer.parseInt(describeMatcher.group(2));
                    tagName = describeMatcher.group(1);
                } else {
                    abbrev = null;
                    distance = 0;
                    tagName = describe;
                }
            }

            return new GitTagDescription(abbrev, tagName, distance);
        } catch (IOException | GitAPIException e) {
            throw new GitRepositoryException(e.getMessage());
        }
    }

    @Override
    public String getHeadRef() {
        return headRef;
    }

    @Override
    public String getAbbreviatedCommitId(GitCommit commit) throws GitRepositoryException {
        try (ObjectReader objectReader = repository.getObjectDatabase().newReader()) {
            return objectReader.abbreviate(((JGitCommit) commit).commit).name();
        } catch (IOException e) {
            throw new GitRepositoryException(
                String.format("Commit \"%s\" could not be abbreviated.", commit.getId()),
                e);
        }
    }

    @Override
    public String getBranch() throws GitRepositoryException {
        try {
            Ref ref = repository.getRefDatabase().findRef(headRef);
            return ref == null ? null : Repository.shortenRefName(ref.getTarget().getName());
        } catch (IOException e) {
            throw new GitRepositoryException("Current branch could not be read.", e);
        }
    }

    DescribeCommand getDescribeCommand() {
        return Git.wrap(repository).describe();
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

        try (RevWalk revWalk = getRevWalk()) {
            for (Ref tag : repository.getRefDatabase().getRefsByPrefix(R_TAGS)) {
                try {
                    RevTag revTag = revWalk.lookupTag(tag.getObjectId());
                    RevObject object = revWalk.peel(revTag);
                    if (object instanceof RevCommit) {
                        tags.put(object.getName(), new JGitTag(revTag));
                    }
                } catch (IncorrectObjectTypeException | MissingObjectException ignored) {
                    // Ignore lightweight tags or tags on missing objects
                }
            }
        } catch (IOException e) {
            throw new GitRepositoryException("The tags could not be resolved.", e);
        }

        return tags;
    }

    public File getWorkTree() {
        return repository.getWorkTree();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public boolean isDirty(boolean ignoreUntracked) throws GitRepositoryException {
        try {
            IndexDiff indexDiff = createIndexDiff();
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
    public boolean isOnUnbornBranch() throws GitRepositoryException {
        return getHeadObject().equals(ObjectId.zeroId());
    }

    @Override
    public void loadTag(GitTag tag) throws GitRepositoryException {
        if (tag.isLoaded()) {
            return;
        }

        JGitTag jgitTag = (JGitTag) tag;

        try (RevWalk revWalk = getRevWalk()) {
            revWalk.parseBody(jgitTag.tag);
            jgitTag.taggerIdent = jgitTag.tag.getTaggerIdent();
        } catch (IOException e) {
            throw new GitRepositoryException("Failed to load tag meta data.", e);
        } finally {
            jgitTag.tag.disposeBody();
        }
    }

    @Override
    public void walkCommits(CommitWalkAction action)
            throws GitRepositoryException {
        try (RevWalk revWalk = getRevWalk()) {
            revWalk.markStart(getHeadRevCommit());

            for (RevCommit commit : revWalk) {
                action.execute(new JGitCommit(commit));
            }
        } catch (IOException | RevWalkException e) {
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
            headCommit = repository.parseCommit(getHeadObject());
            return headCommit;
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
    ObjectId getHeadObject() throws GitRepositoryException {
        if (headObject == null) {
            try {
                headObject = repository.resolve(headRef);
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
     * @return A new JGit {@code RevWalk} instance for this repository
     */
    RevWalk getRevWalk() {
        return new RevWalk(repository);
    }

    private void resolveGitDir(File workTree, FileRepositoryBuilder repositoryBuilder) throws GitRepositoryException {
        File foundGitDir = repositoryBuilder.findGitDir(workTree).getGitDir();
        if (foundGitDir == null) {
            throw new GitRepositoryException(workTree + " is not inside a Git repository. Please specify the GIT_DIR separately.");
        }

        try {
            if (directoryContains(workTree, foundGitDir)) {
                repositoryBuilder.setGitDir(foundGitDir);
                repositoryBuilder.setWorkTree(workTree);
            } else {
                resolveLinkedWorkTree(workTree, foundGitDir, repositoryBuilder);
            }
        } catch (IOException e) {
            throw new GitRepositoryException("Failure while resolving GIT_DIR.", e);
        }
    }

    private void resolveLinkedWorkTree(File workTree, File foundGitDir, FileRepositoryBuilder repositoryBuilder) throws IOException {
        if (directoryContains(foundGitDir.getParentFile(), workTree)) {
            repositoryBuilder.setGitDir(foundGitDir);
            repositoryBuilder.setWorkTree(foundGitDir.getParentFile());
        } else {
            File commonDir = new File(foundGitDir, COMMONDIR_FILE);
            String realGitDirPath = readFileToString(commonDir, UTF_8).trim();

            File realGitDir = new File(foundGitDir, realGitDirPath);
            if (!realGitDir.exists()) {
                realGitDir = new File(realGitDirPath);
            }

            File originalGitDirFile = new File(foundGitDir, GITDIR_FILE);
            String originalGitDirPath = readFileToString(originalGitDirFile, UTF_8).trim();
            File originalGitDir = new File(originalGitDirPath);

            if (originalGitDir.exists() && realGitDir.exists()) {
                if (headRef.equals(HEAD)) {
                    File headFile = new File(foundGitDir, HEAD);
                    String rawHead = readFileToString(headFile, UTF_8);
                    headRef = rawHead.trim().replaceFirst(REF_LINK_PREFIX, "");
                }

                repositoryBuilder.setGitDir(realGitDir);
                repositoryBuilder.setIndexFile(new File(foundGitDir, INDEX_FILE));
                repositoryBuilder.setWorkTree(originalGitDir.getParentFile());
            }
        }
    }
}
