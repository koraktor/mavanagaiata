package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * This goal provides the most recent Git tag in the "mavanagaiata.tag" and
 * "mvngit.tag" properties.
 *
 * @author Sebastian Staudt
 * @goal tag
 * @phase initialize
 * @requiresProject
 * @since 0.1.0
 */
public class GitTagMojo extends AbstractGitMojo {

    private RevWalk revWalk;

    private String tag;

    private Map<RevCommit, String> tagCommits;

    /**
     * This will first read all tags and walk the commit hierarchy down from
     * HEAD until it finds one of the tags. The name of that tag is written
     * into "mavanagaiata.tag" and "mvngit.tag" respectively.
     *
     * @throws MojoExecutionException if the tags cannot be read
     */
    public void execute() throws MojoExecutionException {
        try {
            RevCommit head = this.getHead();
            this.revWalk = new RevWalk(this.repository);
            Map<String, Ref> tags = this.repository.getTags();
            this.tagCommits = new HashMap<RevCommit, String>();

            for(Map.Entry<String, Ref> tag : tags.entrySet()) {
                try {
                    RevTag revTag = this.revWalk.parseTag(tag.getValue().getObjectId());
                    RevObject object = revTag.getObject();
                    if(!(object instanceof RevCommit)) {
                        continue;
                    }
                    this.tagCommits.put((RevCommit) object, tag.getKey());
                } catch(IncorrectObjectTypeException e) {
                    continue;
                }
            }

            String abbrevId = this.repository.getObjectDatabase().newReader()
                .abbreviate(head).name();

            if(this.tagCommits.isEmpty()) {
                this.addProperty("tag.describe", abbrevId);
                this.addProperty("tag.name", "");
                return;
            }

            int distance = this.walkCommits(head, 0);

            if(distance > -1) {
                this.addProperty("tag.name", this.tag);
                if(distance == 0) {
                    this.addProperty("tag.describe", this.tag);
                } else {
                    this.addProperty("tag.describe", this.tag + "-" + distance + "-g" + abbrevId);
                }
            }
        } catch(IOException e) {
            throw new MojoExecutionException("Unable to read Git tag", e);
        }
    }

    /**
     * Returns whether a specific commit has been tagged
     *
     * If the commit is tagged, the tag's name is saved as property "tag"
     *
     * @param commit The commit to check
     * @see #addProperty(String, String)
     * @return <code>true</code> if this commit has been tagged
     */
    private boolean isTagged(RevCommit commit) {
        if(this.tagCommits.containsKey(commit)) {
            this.tag = this.tagCommits.get(commit);
            return true;
        }

        return false;
    }

    /**
     * Walks the hierarchy of commits beginning with the given commit
     *
     * This method is called recursively until a tagged commit is found or the
     * last commit in the hierarchy is reached.
     *
     * @param commit The commit to start with
     * @param distance The distance walked in the commit hierarchy
     * @return The distance at which the tag has been found, or <code>-1</code>
     *         if no tag is reachable from the given commit
     * @throws IOException if an error occured while reading a commit
     * @throws MissingObjectException if a commit is missing
     * @see #isTagged(RevCommit)
     * @see RevCommit#getParentCount()
     * @see RevCommit#getParents()
     */
    private int walkCommits(RevCommit commit, int distance) throws MissingObjectException, IOException {
        commit = (RevCommit) this.revWalk.peel(commit);

        if(this.isTagged(commit)) {
            return distance;
        }

        for(RevCommit parent : commit.getParents()) {
            int tagDistance = this.walkCommits(parent, distance + 1);
            if(tagDistance > -1) {
                return tagDistance;
            }
        }

        return -1;
    }

}
