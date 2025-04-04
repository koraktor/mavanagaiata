Mavanagaiata
============

[![Maven CI](https://github.com/koraktor/mavanagaiata/actions/workflows/maven.yml/badge.svg)](https://github.com/koraktor/mavanagaiata/actions/workflows/maven.yml)
[![Test Coverage](https://api.codeclimate.com/v1/badges/48f1db9ad5dcfe3b99d1/test_coverage)](https://codeclimate.com/github/koraktor/mavanagaiata/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/48f1db9ad5dcfe3b99d1/maintainability)](https://codeclimate.com/github/koraktor/mavanagaiata/maintainability)
[![Maven Release](https://img.shields.io/maven-central/v/com.github.koraktor/mavanagaiata.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.koraktor%22%20AND%20a%3A%22mavanagaiata%22)
[![Liberapay](https://img.shields.io/liberapay/receives/koraktor.svg?logo=liberapay)](https://liberapay.com/koraktor/donate)

Mavanagaiata – \[maˈvanaˈɡaːjaˈta\] – is a Maven plugin providing information
about the Git repository of your project.

## Usage & Installation

Mavanagaiata is available from the Central Repository and will be automatically
installed by Maven once you add it as a plugin to your project.

To use the Mavanagaiata plugin in your Maven project you will have to include
the plugin in your POM and add the configuration suitable for your needs:

```xml
<project ...>
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.koraktor</groupId>
                <artifactId>mavanagaiata</artifactId>
                <executions>
                    <execution>
                        <id>load-git-branch</id>
                        <goals>
                            <goal>branch</goal>
                            <goal>commit</goal>
                            <goal>tag</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
</project>
```

For more information on the existing goals and their configuration can be found
in the [Plugin Documentation][plugin].

A more complete usage example can be found in Mavanagaiata’s own
[`pom.xml`][pom]. Yes, Mavanagaiata is used to build Mavanagaiata.

## Requirements

 * Java 11 or newer
 * Maven 3.6.3 or newer

## Dependencies

 * Apache Commons IO 2.18.0
 * Apache Commons Lang 3.17.0
 * Apache Commons Text 1.13.0
 * JGit 6.10.0
 * Maven Filtering 3.4.0

## Contribute

Mavanagaiata is an open-source project. Therefore you are free to help
improving it. There are several ways of contributing to Mavanagaiata’s
development:

* Build projects using it and spread the word.
* Report problems and request features using the [issue tracker][issues].
* Write patches yourself to fix bugs and implement new functionality.
* Create a Mavanagaiata fork on [GitHub][github] and start hacking. Extra
  points for using GitHub’s pull requests and feature branches.

If you want to hack on the code you are free to clone the Git repository. You
can do so using the following commands:

```bash
$ git clone git://github.com/koraktor/mavanagaiata.git
$ cd mavanagaiata
$ mvn install
```

## About the name

The name is a completely invented word hopefully sounding like a mighty god of
an ancient, Southeast Asian primitive people or a similar mighty monster that
same primitive people is afraid of.

Instead, it’s just a combination of the command-line tools of Maven and Git:
`mvn` and `git`. Each character is suffixed with the character `a`.

In Java code you would write this as:

```java
("mvn" + "git").replaceAll("(.)", "$1a")
=> "mavanagaiata"
```

## License

This code is free software; you can redistribute it and/or modify it under the
terms of the new BSD License. A copy of this license can be found in the
included LICENSE file.

## Credits

* Sebastian Staudt -- koraktor(at)gmail.com
* Pablo Graña -- pablo.grana(at)globant.com
* Henning Schmiedehausen -- hgschmie(at)fb.com
* Santeri Vesalainen -- santeri.vesalainen(at)gmail.com
* Patrick Kaeding -- pkaeding(at)atlassian.com
* Kay Hannay -- klinux(at)hannay.de
* Jeff Kreska -- jeff.kreska(at)farecompare.com
* Jeremy Landis -- jeremylandis(at)hotmail.com

## See Also

* [Mavanagaiata home](https://koraktor.de/mavanagaiata)
* [GitHub project page][github]
* [Open Hub profile](https://www.openhub.net/p/mavanagaiata)

 [github]: https://github.com/koraktor/mavanagaiata
 [issues]: https://github.com/koraktor/mavanagaiata/issues
 [plugin]: https://koraktor.de/mavanagaiata/plugin-info.html
 [pom]: https://github.com/koraktor/mavanagaiata/blob/master/pom.xml
