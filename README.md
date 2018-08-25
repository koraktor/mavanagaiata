Mavanagaiata
============

[![Build Status](https://travis-ci.org/koraktor/mavanagaiata.svg?branch=master)](https://travis-ci.org/koraktor/mavanagaiata)
[![Test Coverage](https://api.codeclimate.com/v1/badges/48f1db9ad5dcfe3b99d1/test_coverage)](https://codeclimate.com/github/koraktor/mavanagaiata/test_coverage)
[![Maintainability](https://api.codeclimate.com/v1/badges/48f1db9ad5dcfe3b99d1/maintainability)](https://codeclimate.com/github/koraktor/mavanagaiata/maintainability)
[![Maven Release](https://img.shields.io/maven-central/v/com.github.koraktor/mavanagaiata.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.koraktor%22%20AND%20a%3A%22mavanagaiata%22)

Mavanagaiata – \[maˈvanaˈɡaːjaˈta\] – is a Maven plugin providing information
about the Git repository of your project.

## Requirements

 * Maven 3.0.5 or newer

## Dependencies

 * Apache Commons IO 2.6
 * Apache Commons Lang 3.7
 * Apache Commons Text 1.4
 * JGit 5.0.2
 * Maven Filtering 3.1.1

## Installation

Mavanagaiata is available from the Central Repository and will be automatically
installed by Maven once you add it as a plugin to your project. If you want to
have the newest features available in the development code or you want to hack
on the code you are free to clone the Git repository and install it manually.
You can do so using the following commands:

```bash
$ git clone git://github.com/koraktor/mavanagaiata.git
$ cd mavanagaiata
$ mvn install
```

## Usage

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
in the [Plugin Documentation][3].

A more complete usage example can be found in Mavanagaiata’s own [`pom.xml`][4].
Yes, Mavanagaiata is used to build Mavanagaiata.

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

## Contribute

Mavanagaiata is an open-source project. Therefore you are free to help
improving it. There are several ways of contributing to Mavanagaiata’s
development:

* Build projects using it and spread the word.
* Report problems and request features using the [issue tracker][2].
* Write patches yourself to fix bugs and implement new functionality.
* Create a Mavanagaiata fork on [GitHub][1] and start hacking. Extra points for
  using GitHub’s pull requests and feature branches.

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

## See Also

* [Mavanagaiata home](https://koraktor.de/mavanagaiata)
* [GitHub project page][1]
* [Gitter chat](https://gitter.im/koraktor/mavanagaiata)
* [Google group](http://groups.google.com/group/mavanagaiata)
* [Open Hub profile](https://www.openhub.net/p/mavanagaiata)

 [1]: https://github.com/koraktor/mavanagaiata
 [2]: https://github.com/koraktor/mavanagaiata/issues
 [3]: http://koraktor.de/mavanagaiata/plugin-info.html
 [4]: https://github.com/koraktor/mavanagaiata/blob/master/pom.xml
