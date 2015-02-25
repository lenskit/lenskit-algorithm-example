# LensKit Example Algorithm

This demo extends [lenskit-hello][] to use a custom recommender algorithm.  This
example is a user-item personalized mean recommender.
 
This is intended to serve as a starting point and example for building your own
custom algorithms.  If you want to just use a user-item personalized mean scorer
for recommendation, use LensKit's `UserMeanItemScorer`.

[lenskit-hello]: https://github.com/lenskit/lenskit-hello

## Project Setup

This project uses [Gradle][gradle] for build and dependency management. It is
easy to import into an IDE; Gradle support is included with or available for
NetBeans, IntelliJ IDEA, and Eclipse.  These IDEs will import your project directly
from the Gradle `build.gradle` file and set up the build and dependencies.

The `build.gradle` file contains the project definition and its dependencies. Review
this for how we pull in LensKit, and how to depend on other modules.

## Building and Running

In the Gradle build, we use the Application plugin to create a shell script and copy
the dependency JARs in order to run the LensKit application.

[ML100K]: https://github.com/grouplens/lenskit/wiki/ML100K

You'll also need a data set.  You can get the MovieLens 100K data set [here][ML100K].

Once you have a data set, you can run lenskit-hello through your IDE, or from the command line
as follows:

    $ ./gradlew build
    $ /bin/sh target/hello/bin/lenskit-hello.sh ml100k/u.data <userid>

The default delimiter is the tab character.

[LensKit]: http://lenskit.grouplens.org
[gradle]: http://gradle.org
[MercurialEclipse]: http://javaforge.com/project/HGE
[AppAssembler]: http://mojo.codehaus.org/appassembler/appassembler-maven-plugin/
[mailing list]: https://wwws.cs.umn.edu/mm-cs/listinfo/lenskit
[LensKitRS]: http://twitter.com/LensKitRS

## Understanding the Code

The custom algorithm code lives in the `org.grouplens.lenskit.hello` package.

The algorithm implements the formula:

    s(i,j) = global_mean + item_bias + user_bias

The custom algorithm consists of three classes:

-   The item scorer (`UserItemBiasItemScorer`).  Most LensKit algorithms are based on a
    new ItemScorer implementation.
-   The *model*, `BiasModel`, that stores the global mean and item biases.  It is expensive
    to recompute item averages every time that recommendations are needed, so the code precomputes
    them in a model.  Many recommenders will have some kind of model that gets pre-computed.
-   The *model builder* `BiasModelBuilder`.  This class takes the data and computes the model.

### Item Scorer

LensKit algorithms usually start with a custom item scorer, implementing the `ItemScorer` interface.
The item scorer computes user-personalized scores for items, that can then be used to rank items
for recommendation or to compute rating predictions.

`ItemScorer` has 3 different methods that allow it to be used in different ways.  The base class
`AbstractItemScorer` simplifies the work of writing an item scorer; classes that inherit from it
need to implement one of the methods.

`UserItemBiasItemScorer` is the custom item scorer implementation.  Some things to note:
 
-   Its constructor is annotated with `@Inject`.  This tells LensKit that it can use the
    the constructor to create instances of the item scorer.  This annotation needs to be on exactly
    one constructor for class that implements a piece of a LensKit algorithm (we call these pieces
    *components*).

-   The constructor has two parameters.  These parameters are *dependencies*, and LensKit will make
    sure that objects of the appropriate type are available and provide them as constructor 
    arguments.  This lets LensKit algorithms be flexible and modular, without requiring a lot of
    manual code in order to work.  If a component needs some other component in order to function,
    it just ‘asks’ for it as a constructor argument, and LensKit provides it (or raises an error).
    
-   The item scorer uses several vector operations to accumulate the scores.  See the [sparse vector
    documentation][SV] for more on how to work with sparse vectors.  The `scores` parameter to the
    `score` method is both an input and output parameter:  the *key domain* of the vector has the
    items to score, and the `score` method stores the scores back in the vector.

[SV]: https://github.com/lenskit/lenskit/wiki/SparseVectors

### Model

Many algorithms have some kind of data that will be precomputed once from the data being used
for recommendation, and then used to make many recommendations.  For example, a matrix factorization
recommender will need to compute the factorization of the rating matrix.  This is often an expensive
computation that will happen periodically (e.g. nightly).

In this example, the model (`BiasModel`) just stores the global mean rating and each item's bias (the 
difference between its average rating and the global average).

The model is annotated with the `@Shareable` annotation, telling LensKit that it can be shared and
reused.  It is also serializable, a requirement for shareable components.

It is also annotated with `@DefaultBuilder`, telling LensKit to use the `BiasModelBuilder` class to
build the model.  LensKit algorithms usually use a separate class to build models rather than
performing the computation in the constructor, so that constructors stay simple and to provide a
clean division between code that runs in advance to compute the recommendation model, code that uses
the model, and the model itself that bridges between the two.

### Model Builder

The model builder is itself a component (class) that implements the `Provider` interface.  In this
example, it is `BiasModelBuilder`.

It has an `@Inject` constructor; in this case, it just requires the `EventDAO` so it can get all
the ratings.  This dependency is annotated with `@Transient`, because the builder uses the DAO to
compute averages that will be stored in the model, but the DAO is only used in the process of
building the model and is not used by the model itself.

### Using the Recommender

In this project, the `HelloLenskit` class has been modified to use our custom item scorer instead
of item-item CF.  It does this by *binding* the `ItemScorer` class to `UserItemBiasItemScorer`.

LensKit then does several things automatically:

1. Create an instance of the model builder, using the configured data access object.
2. Tell the model builder to create a model.
3. Create an item scorer, using the model and a user-event DAO (which is built automatically from
   the event DAO that is configured in `HelloLenskit`).
