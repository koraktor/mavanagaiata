/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2014-2018, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of Git's {@code .mailmap} functionality
 *
 * @author Sebastian Staudt
 */
public class MailMap {

    static final String MAILMAP_FILE = ".mailmap";
    private static final Pattern MAIL_TO_MAIL_PATTERN;
    private static final Pattern MAIL_TO_NAME_PATTERN;
    private static final Pattern MAIL_TO_NAME_AND_MAIL_PATTERN;
    private static final Pattern NAME_AND_MAIL_TO_NAME_AND_MAIL_PATTERN;

    static {
        MAIL_TO_MAIL_PATTERN = Pattern.compile("^<(\\S+)>\\s+<(\\S+)>$");
        MAIL_TO_NAME_PATTERN = Pattern.compile("^(\\S.*?)\\s+<(\\S+)>$");
        MAIL_TO_NAME_AND_MAIL_PATTERN = Pattern.compile("^(\\S.*?)\\s+<(\\S+)>\\s+<(\\S+)>$");
        NAME_AND_MAIL_TO_NAME_AND_MAIL_PATTERN = Pattern.compile("^(\\S.*?)\\s+<(\\S+)>\\s+(\\S.*?)\\s+<(\\S+)>$");
    }

    boolean exists = false;

    Map<String, String> mailToMailMap;

    Map<String, String> mailToNameMap;

    Map<String, Map.Entry<String, String>> mailToNameAndMailMap;

    Map<Map.Entry<String, String>, Map.Entry<String, String>> nameAndMailToNameAndMailMap;

    GitRepository repository;

    /**
     * Creates a new mail map instance
     *
     * @param repository The Git repository to parse the mail map for
     * @see #parseMailMap
     */
    MailMap(GitRepository repository) {
        this.repository = repository;

        mailToMailMap = new HashMap<>();
        mailToNameMap = new HashMap<>();
        mailToNameAndMailMap = new HashMap<>();
        nameAndMailToNameAndMailMap = new HashMap<>();
    }

    /**
     * Returns whether a mail map has been found for the repository
     *
     * @return {@code true} if the mail map has been parsed from an existing
     *         {@code .mailmap} file
     * @see #parseMailMap
     */
    public boolean exists() {
        return this.exists;
    }

    /**
     * Returns the canonical email address for the given name and email address
     * pair
     *
     * @param name The actual name from a commit
     * @param mail The actual email address from a commit
     * @return The email address matching a mapping in the mail map or the
     *         initial email address
     */
    String getCanonicalMail(String name, String mail) {
        if (this.mailToMailMap.containsKey(mail)) {
            return this.mailToMailMap.get(mail);
        }

        if (this.mailToNameAndMailMap.containsKey(mail)) {
            return this.mailToNameAndMailMap.get(mail).getValue();
        }

        Map.Entry<String, String> nameAndMail = new AbstractMap.SimpleEntry<>(name, mail);
        if (this.nameAndMailToNameAndMailMap.containsKey(nameAndMail)) {
            return this.nameAndMailToNameAndMailMap.get(nameAndMail).getValue();
        }

        return mail;
    }

    /**
     * Returns the canonical name for the given name and email address pair
     *
     * @param name The actual name from a commit
     * @param mail The actual email address from a commit
     * @return The name matching a mapping in the mail map or the initial name
     */
    String getCanonicalName(String name, String mail) {
        if (this.mailToNameMap.containsKey(mail)) {
            return this.mailToNameMap.get(mail);
        }

        if (this.mailToNameAndMailMap.containsKey(mail)) {
            return this.mailToNameAndMailMap.get(mail).getKey();
        }

        Map.Entry<String, String> nameAndMail = new AbstractMap.SimpleEntry<>(name, mail);
        if (this.nameAndMailToNameAndMailMap.containsKey(nameAndMail)) {
            return this.nameAndMailToNameAndMailMap.get(nameAndMail).getKey();
        }

        return name;
    }

    /**
     * Returns the canonical email address of the author of the given commit
     * object
     *
     * @param commit The commit object to get the email address from
     * @return The canonical email address of the author
     * @see #getCanonicalMail(String, String)
     */
    public String getCanonicalAuthorEmailAddress(GitCommit commit) {
        return this.getCanonicalMail(commit.getAuthorName(), commit.getAuthorEmailAddress());
    }

    /**
     * Returns the canonical name of the author of the given commit
     * object
     *
     * @param commit The commit object to get the name from
     * @return The canonical name of the author
     * @see #getCanonicalName(String, String)
     */
    public String getCanonicalAuthorName(GitCommit commit) {
        return this.getCanonicalName(commit.getAuthorName(), commit.getAuthorEmailAddress());
    }

    /**
     * Returns the canonical email address of the committer of the given commit
     * object
     *
     * @param commit The commit object to get the email address from
     * @return The canonical email address of the author
     * @see #getCanonicalMail(String, String)
     */
    public String getCanonicalCommitterEmailAddress(GitCommit commit) {
        return this.getCanonicalMail(commit.getCommitterName(), commit.getCommitterEmailAddress());
    }

    /**
     * Returns the canonical name of the committer of the given commit
     * object
     *
     * @param commit The commit object to get the name from
     * @return The canonical name of the author
     * @see #getCanonicalName(String, String)
     */
    public String getCanonicalCommitterName(GitCommit commit) {
        return this.getCanonicalName(commit.getCommitterName(), commit.getCommitterEmailAddress());
    }

    /**
     * Tries to parse the {@code .mailmap} file from the worktree of the given
     * repository.
     * <br>
     * If the file exists and contains valid content {@link #exists()} will
     * return {@code true}.
     *
     * @see #parseMailMap(File)
     * @throws GitRepositoryException if the {@code .mailmap} file cannot be
     *         read
     */
    void parseMailMap() throws GitRepositoryException {
        File mailMap = new File(repository.getWorkTree(), MAILMAP_FILE);

        try {
            parseMailMap(mailMap);
            exists = !(mailToMailMap.isEmpty() &&
                    mailToNameMap.isEmpty() &&
                    mailToNameAndMailMap.isEmpty() &&
                    nameAndMailToNameAndMailMap.isEmpty());
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            throw new GitRepositoryException("Error while parsing the .mailmap.", e);
        }
    }

    /**
     * Tries to parse the given file using the rules from
     * <a href="http://git-scm.com/docs/git-shortlog">git-shortlog</a>.
     * <br>
     * Lines not matching one of the valid formats are silently ignored.
     *
     * @param mailMap The {@code .mailmap} file to parse
     * @throws FileNotFoundException if the {@code .mailmap} file does not
     *         exist
     * @throws IOException if the {@code .mailmap} file cannot be read
     */
    void parseMailMap(File mailMap) throws IOException {
        try (BufferedReader mailMapReader = new BufferedReader(new FileReader(mailMap))) {
            String line;
            while ((line = mailMapReader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                Matcher lineMatcher = NAME_AND_MAIL_TO_NAME_AND_MAIL_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    Map.Entry<String, String> properNameAndMail = new AbstractMap.SimpleEntry<>(lineMatcher.group(1), lineMatcher.group(2));
                    Map.Entry<String, String> commitNameAndMail = new AbstractMap.SimpleEntry<>(lineMatcher.group(3), lineMatcher.group(4));
                    nameAndMailToNameAndMailMap.put(commitNameAndMail, properNameAndMail);
                    continue;
                }

                lineMatcher = MAIL_TO_NAME_AND_MAIL_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    Map.Entry<String, String> properNameAndMail = new AbstractMap.SimpleEntry<>(lineMatcher.group(1), lineMatcher.group(2));
                    mailToNameAndMailMap.put(lineMatcher.group(3), properNameAndMail);
                    continue;
                }

                lineMatcher = MAIL_TO_MAIL_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    mailToMailMap.put(lineMatcher.group(2), lineMatcher.group(1));
                    continue;
                }

                lineMatcher = MAIL_TO_NAME_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    mailToNameMap.put(lineMatcher.group(2), lineMatcher.group(1));
                }
            }
        }
    }

}
