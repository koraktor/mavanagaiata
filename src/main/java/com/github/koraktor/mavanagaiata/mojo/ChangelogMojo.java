/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2011-2019, Sebastian Staudt
 *               2016, Jeff Kreska
 */

package com.github.koraktor.mavanagaiata.mojo;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.koraktor.mavanagaiata.git.CommitWalkAction;
import com.github.koraktor.mavanagaiata.git.GitRepository;
import com.github.koraktor.mavanagaiata.git.GitRepositoryException;
import com.github.koraktor.mavanagaiata.git.GitTag;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * This goal allows to generate a changelog of the currently checked out branch
 * of the Git repository. It will use information from tags and commit messages
 * to build a reverse chronological summary of the development. It can be
 * configured to display the changelog or save it to a file.
 *
 * @author Sebastian Staudt
 * @since 0.2.0
 */
@Mojo(name = "changelog",
      defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
      threadSafe = true)
public class ChangelogMojo extends AbstractGitOutputMojo {

    /**
     * Pre-defined base URLs used for links
     */
    enum LinkToBaseUrl {
        GITHUB("https://github.com/%s/%s"),
        GITLAB("https://gitlab.com/%s/%s");

        private final String baseUrl;

        LinkToBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    /**
     * The format to use while generating the changelog
     *
     * @see #formatTemplate
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.format")
    protected ChangelogFormat format;

    /**
     * The formatting template to use while generating the changelog
     * <p>
     * This may be one of {@code DEFAULT} or {@code MARKDOWN}.
     * <p>
     * Individual attributes may be overridden using {@link #format}.
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.formatTemplate",
               defaultValue = "DEFAULT")
    ChangelogFormat.Formats formatTemplate;

    /**
     * The project name for GitHub links
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.linkToProject")
    String linkToProject;

    /**
     * The username for GitHub links
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.linkToUser")
    String linkToUser;

    /**
     * Used to select the service to create links to
     * <p>
     * {@code GITHUB} and {@code GITLAB} are available.
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.linkTo",
               defaultValue = "GITHUB")
    LinkToBaseUrl linkTo;

    /**
     * Can be used to override the pre-defined URLs from {@link #linkTo} with
     * a customized URL
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.linkToBaseUrl")
    String linkToBaseUrl;

    /**
     * The file to write the changelog to
     *
     * @since 0.4.1
     */
    @Parameter(property = "mavanagaiata.changelog.outputFile")
    protected File outputFile;

    /**
     * Whether to skip merge commits’ messages
     *
     * @since 0.9.0
     */
    @Parameter(property = "mavanagaiata.changelog.skipMergeCommits",
               defaultValue = "true")
    boolean skipMergeCommits;

    /**
     * Whether to skip tagged commits' messages
     * <br>
     * This is useful when usually tagging commits like "Version bump to X.Y.Z"
     */
    @Parameter(property = "mavanagaiata.changelog.skipTagged",
               defaultValue = "false")
    boolean skipTagged;

    /**
     * Whether to skip commits that match the given regular expression
     *
     * @since 0.8.0
     */
    @Parameter(property = "mavanagaiata.changelog.skipCommitsMatching")
    String skipCommitsMatching;

    /**
     * Whether to trim trailing whitespace from commits.
     * <p>
     * This is useful for cases where auto formatting used and changelog checked in.
     *
     * @since 1.1.0
     */
    @Parameter(property = "mavanagaiata.changelog.trimTrailingWhitespace")
    boolean trimTrailingWhitespace;

    private Pattern skipCommitsPattern;

    /**
     * Walks through the history of the currently checked out branch of the
     * Git repository and builds a changelog from the commits contained in that
     * branch.
     *
     * @throws MavanagaiataMojoException if retrieving information from the Git
     *         repository fails
     */
    @Override
    protected void writeOutput(GitRepository repository)
            throws MavanagaiataMojoException {
        try {
            format.printStream = printStream;
            format.printHeader();

            ChangelogWalkAction action = new ChangelogWalkAction(repository);
            action.currentRef = repository.getBranch();
            repository.walkCommits(action);

            format.printSeparator();
            format.printCompareLink(action.currentRef, null, action.currentRef.equals(repository.getBranch()));
        } catch (GitRepositoryException e) {
            throw MavanagaiataMojoException.create("Unable to generate changelog from Git", e);
        }
    }

    /**
     * Returns the output file for the generated changelog
     *
     * @return The output file for the generated changelog
     */
    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    protected void initConfiguration() {
        super.initConfiguration();

        format = formatTemplate.getFormat().apply(format);
        if (format.dateFormat == null) {
            format.dateFormat = dateFormat;
        }
        format.prepare();

        if (isNotBlank(linkToUser) && isNotBlank(linkToProject)) {
            String baseUrl = isNotBlank(linkToBaseUrl) ? linkToBaseUrl : linkTo.baseUrl;
            format.enableCreateLinks(String.format(baseUrl, linkToUser, linkToProject));
        }

        if (skipCommitsMatching != null && !skipCommitsMatching.isEmpty()) {
            skipCommitsPattern = Pattern.compile(skipCommitsMatching, Pattern.MULTILINE);
        }
    }

    /**
     * Sets the output file for the generated changelog
     *
     * @param outputFile The output file for the generated changelog
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    class ChangelogWalkAction extends CommitWalkAction {

        private String currentRef;
        private boolean firstCommit = true;
        private GitRepository repository;
        private Map<String, GitTag> tags;

        ChangelogWalkAction(GitRepository repository) throws GitRepositoryException {
            this.repository = repository;
            tags = repository.getTags();
        }

        protected void run() throws GitRepositoryException {
            if (skipCommitsPattern != null && skipCommitsPattern.matcher(currentCommit.getMessage()).find()) {
                return;
            }

            if (skipMergeCommits && currentCommit.isMergeCommit()) {
                return;
            }

            boolean firstLine = firstCommit;
            firstCommit = false;

            if (tags.containsKey(currentCommit.getId())) {
                String lastRef = currentRef;
                GitTag currentTag = tags.get(currentCommit.getId());
                currentRef = currentTag.getName();

                format.printSeparator();

                if (!firstLine) {
                    format.printCompareLink(currentRef, lastRef, lastRef.equals(repository.getBranch()));
                }

                repository.loadTag(currentTag);
                format.printTag(currentTag);

                if (skipTagged) {
                    return;
                }
            } else if (firstLine) {
                format.printBranch(repository.getBranch());
            }

            format.printCommit(currentCommit, trimTrailingWhitespace);
        }

    }

}
