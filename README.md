# LensKit Example Algorithm

This demo extends [lenskit-hello][] to use a custom recommender algorithm.  This
example is a user-item personalized mean recommender.
 
This is intended to serve as a starting point and example for building your own
custom algorithms.  If you want to just use a user-item personalized mean scorer
for recommendation, use LensKit's `UserMeanItemScorer`.

[lenskit-hello]: https://github.com/lenskit/lenskit-hello

## Project Setup

This project uses [Apache Maven][maven] for build and dependency management. It is
easy to import into an IDE; a Maven plugin for Eclipse is available in the Eclipse
Marketplace (in your *Help* menu), and NetBeans and IntelliJ IDEA both have Maven
support built-in. These IDEs will import your project directly from the Maven `pom.xml`
and set up the build and dependencies.

**Note**: If you import the Mercurial repository into Eclipse using the [MercurialEclipse][]
plugin, you'll need to convert it to a Maven project (right-click the project, select
*Configure -> Convert to Maven Project*) after importing.

The `pom.xml` file contains the project definition and its dependencies. Review this
for how we pull in LensKit, and how to depend on other modules.

## Building and Running

In the Maven POM, we have set up the [AppAssembler plugin][] to produce a runnable
application with shell scripts and batch files to launch it. To build this, run the
`package` Maven target.

[ML100K]: https://github.com/grouplens/lenskit/wiki/ML100K

You'll also need a data set.  You can get the MovieLens 100K data set [here][ML100K].

Once you have a data set, you can run lenskit-hello through your IDE, or from the command line
(with Maven installed) as follows:

    $ mvn package
    $ /bin/sh target/hello/bin/lenskit-hello.sh ml100k/u.data <userid>

The default delimiter is the tab character.

Have fun!

[LensKit]: http://lenskit.grouplens.org
[maven]: http://maven.apache.org
[MercurialEclipse]: http://javaforge.com/project/HGE
[AppAssembler]: http://mojo.codehaus.org/appassembler/appassembler-maven-plugin/
[mailing list]: https://wwws.cs.umn.edu/mm-cs/listinfo/lenskit
[LensKitRS]: http://twitter.com/LensKitRS