Mavanagaiata
============

Mavanagaiata – \[maˈvanaˈɡaːjaˈta\] – is a Maven plugin providing information
about the Git repository of your project.

## Requirements

 * Maven 2.0+

## Installation

Mavanagaiata is not yet hosted in a public Maven repository, so the only way to
install it at the moment is to clone the Git repository and install it
manually. You can do so using the following commands:

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
                <version>0.0.0</version>
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
