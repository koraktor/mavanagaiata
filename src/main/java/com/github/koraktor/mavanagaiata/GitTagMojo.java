package com.github.koraktor.mavanagaiata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
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
 */
public class GitTagMojo extends AbstractGitMojo {

    private Map<String, RevCommit> tagCommits;

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
            RevWalk revWalk = new RevWalk(this.repository);
            Map<String, Ref> tags = this.repository.getTags();
            this.tagCommits = new HashMap<String, RevCommit>();

            for(Map.Entry<String, Ref> tag : tags.entrySet()) {
                try {
                    RevTag revTag = revWalk.parseTag(tag.getValue().getObjectId());
                    RevObject object = revTag.getObject();
                    if(!(object instanceof RevCommit)) {
                        continue;
                    }
                    this.tagCommits.put(tag.getKey(), (RevCommit) object);
                } catch(IncorrectObjectTypeException e) {
                    continue;
                }
            }

            if(this.tagCommits.isEmpty()) {
                this.addProperty("tag", "");
                return;
            }

            this.walkCommits(head);
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
     * @see #addProperty(String, Object)
     * @return <code>true</code> if this commit has been tagged
     */
    private boolean isTagged(RevCommit commit) {
        if(this.tagCommits.containsValue(commit)) {
            for(Map.Entry<String, RevCommit> tagCommit : this.tagCommits.entrySet()) {
                if(tagCommit.getValue().equals(commit)) {
                    this.addProperty("tag", tagCommit.getKey());
                    return true;
                }
            }
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
     * @see #isTagged(RevCommit)
     * @see RevCommit#getParentCount()
     * @see RevCommit#getParents()
     */
    private void walkCommits(RevCommit commit) {
        if(isTagged(commit) || commit.getParentCount() == 0) {
            return;
        }

        for(RevCommit parent : commit.getParents()) {
            this.walkCommits(parent);
        }
    }

}
