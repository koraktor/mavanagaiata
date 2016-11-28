/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2014, Sebastian Staudt
 */

package com.github.koraktor.mavanagaiata.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.Map;

import org.junit.Test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Staudt
 */
public class MailMapTest {

    @Test
    public void testGetCanonicalMail() {
        MailMap mailMap = new MailMap();
        mailMap.mailToMailMap.put("oldmail@example.com", "newmail@example.com");
        mailMap.mailToNameAndMailMap.put("oldmail2@example.com", new AbstractMap.SimpleEntry<>("Test", "newmail2@example.com"));
        mailMap.nameAndMailToNameAndMailMap.put(new AbstractMap.SimpleEntry<>("Test", "oldmail3@example.com"), new AbstractMap.SimpleEntry<>("Test", "newmail3@example.com"));

        assertThat(mailMap.getCanonicalMail("Test", "oldmail@example.com"), is(equalTo("newmail@example.com")));
        assertThat(mailMap.getCanonicalMail("Test", "oldmail2@example.com"), is(equalTo("newmail2@example.com")));
        assertThat(mailMap.getCanonicalMail("Test", "oldmail3@example.com"), is(equalTo("newmail3@example.com")));
        assertThat(mailMap.getCanonicalMail("Test", "unknown@example.com"), is(equalTo("unknown@example.com")));
    }

    @Test
    public void testGetCanonicalName() {
        MailMap mailMap = new MailMap();
        mailMap.mailToNameMap.put("mail1@example.com", "Test 1");
        mailMap.mailToNameAndMailMap.put("mail2@example.com", new AbstractMap.SimpleEntry<>("Test 2", "mail@example.com"));
        mailMap.nameAndMailToNameAndMailMap.put(new AbstractMap.SimpleEntry<>("Test", "mail3@example.com"), new AbstractMap.SimpleEntry<>("Test 3", "mail@example.com"));

        assertThat(mailMap.getCanonicalName("Test", "mail1@example.com"), is(equalTo("Test 1")));
        assertThat(mailMap.getCanonicalName("Test", "mail2@example.com"), is(equalTo("Test 2")));
        assertThat(mailMap.getCanonicalName("Test", "mail3@example.com"), is(equalTo("Test 3")));
        assertThat(mailMap.getCanonicalName("Unknown", "mail@example.com"), is(equalTo("Unknown")));
    }

    @Test
    public void testNewInstance() {
        MailMap mailMap = new MailMap();

        assertThat(mailMap.exists, is(false));
        assertThat(mailMap.mailToMailMap.isEmpty(), is(true));
        assertThat(mailMap.mailToNameMap.isEmpty(), is(true));
        assertThat(mailMap.mailToNameAndMailMap.isEmpty(), is(true));
        assertThat(mailMap.nameAndMailToNameAndMailMap.isEmpty(), is(true));
    }

    @Test
    public void testExists() {
        MailMap mailMap = new MailMap();

        assertThat(mailMap.exists(), is(false));

        mailMap.exists = true;

        assertThat(mailMap.exists(), is(true));
    }

    @Test
    public void testParseFromRepository() throws Exception {
        final MailMap mailMap = spy(new MailMap());
        GitRepository repo = mock(GitRepository.class);
        when(repo.getWorkTree()).thenReturn(new File("test"));

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mailMap.mailToMailMap.put("test", "test");
                return null;
            }
        }).when(mailMap).parseMailMap(eq(new File("test/.mailmap")));

        mailMap.parseMailMap(repo);

        assertThat(mailMap.exists(), is(true));
    }

    @Test
    public void testParseFromRepositoryWithoutMailmap() throws Exception {
        MailMap mailMap = spy(new MailMap());
        GitRepository repo = mock(GitRepository.class);
        when(repo.getWorkTree()).thenReturn(new File("test"));

        doThrow(new FileNotFoundException()).when(mailMap).
                parseMailMap(eq(new File("test/.mailmap")));

        mailMap.parseMailMap(repo);

        assertThat(mailMap.exists(), is(false));
    }

    @Test
    public void testParseFromFile() throws Exception {
        MailMap mailMap = new MailMap();
        File mailMapFile = new File(this.getClass().getResource("/.mailmap").getFile());

        mailMap.parseMailMap(mailMapFile);

        assertThat(mailMap.mailToMailMap.size(), is(1));
        assertThat(mailMap.mailToMailMap.get("oldmail@example.com"), is(equalTo("newmail@example.com")));
        assertThat(mailMap.mailToNameMap.size(), is(1));
        assertThat(mailMap.mailToNameMap.get("realmail@example.com"), is(equalTo("Real Name")));
        assertThat(mailMap.mailToNameAndMailMap.size(), is(1));
        assertThat(mailMap.mailToNameAndMailMap.get("oldmail@example.com"), is(equalTo((Map.Entry<String, String>) new AbstractMap.SimpleEntry<>("Real Name", "newmail@example.com"))));
        assertThat(mailMap.nameAndMailToNameAndMailMap.size(), is(1));
        assertThat(mailMap.nameAndMailToNameAndMailMap.get(new AbstractMap.SimpleEntry<>("Fake Name", "oldmail@example.com")), is(equalTo((Map.Entry<String, String>) new AbstractMap.SimpleEntry<>("Real Name", "newmail@example.com"))));
    }

}
