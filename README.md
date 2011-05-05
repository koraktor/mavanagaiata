Mavanagaiata
============

Mavanagaiata – \[maˈvanaˈɡaːjaˈta\] – is a Maven plugin providing information
about the Git repository of your project.

## Requirements

 * Maven 2.0+

## Dependencies

 * JGit 0.12.1

## Installation

Mavanagaiata is available from the Central Reposioty and will be automatically
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

### Goals

Mavangaiata provides the following goals each reading specific information from
the Git repository.

 * `actors`:    Information about the actors of the current commit
 * `branch`:    Information about the currently checked out branch
 * `changelog`: Generates a changelog from Git commits and tags
 * `commit`:    Information about the current commit
 * `tag`:       Information about the most recent tag

Each goal stores its information into the project's properties. The following
property keys will be prefixed with `mavanagaiata.` and `mvngit.` respectively.
You may override this with the configuration property `propertyPrefixes`.

 * `actors`
   * `author.name`:     The name of the author of the current commit
   * `author.email`:    The email address of the author of the current commit
   * `committer.name`:  The name of the committer of the current commit
   * `committer.email`: The email address of the committer of the current
     commit
 * `branch`
   * `branch`: The name of the currently checked out branch
 * `commit`
   * `abbrev`:     The abbreviated SHA ID of the current commit
   * `id` / `sha`: The full SHA ID of the current commit
   * `date`:       A string representation of the date of the current commit
 * `tag`
   * `name`:     The name of the most recent tag (if any)
   * `describe`: A combination of the tag name and the current commit ID
     (same as `git describe`)

## About the name

The name is a completely invented word hopefully sounding like a mighty god of
an ancient, Southeast Asian primitive people or a similar mighty monster that
same primitive people is afraid of.

Instead, it's just a combination of the command-line tools of Maven and Git:
`mvn` and `git`. Each character is suffixed with the character `a`.

In Java code you would write this as:

```java
("mvn" + "git").replaceAll("(.)", "$1a")
=> "mavanagaiata"
```

## Contribute

Mavanagaiata is an open-source project. Therefore you are free to help
improving it. There are several ways of contributing to Mavanagaiata's
development:

* Build projects using it and spread the word.
* Report problems and request features using the [issue tracker][2].
* Write patches yourself to fix bugs and implement new functionality.
* Create a Mavanagaiata fork on [GitHub][1] and start hacking. Extra points for
  using GitHub's pull requests and feature branches.

## License

This code is free software; you can redistribute it and/or modify it under the
terms of the new BSD License. A copy of this license can be found in the
included LICENSE file.

## Credits

* Sebastian Staudt -- koraktor(at)gmail.com

 [1]: https://github.com/koraktor/mavanagaiata
 [2]: https://github.com/koraktor/mavanagaiata/issues
