ceylon-maven-plugin
===================

Maven Plugin for Ceylon 1.0.0+


This plugin implements a `car` packaging type with a custom lifecycle currently including the following goals

* compile
* run
* test-compile
* test

The `compile`, `test-compile` and `test` goals are bound to the corresponding lifecycle phase.

The plugin requires a Ceylon SDK to be installed, it launches the `ceylon` tool from the SDK, much like the Ceylon Ant tasks do.

The Ceylon SDK home must be set as property `ceylon.home`. The environment variable `CEYLON_HOME` will be used as fallback when the property is not set.


Building the plugin
-------------------

Clone this repository and run `mvn install`.

Using the plugin
----------------

Have a look at the integration tests in `src/it/*`.
