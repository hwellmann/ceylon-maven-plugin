ceylon-maven-plugin
===================

Maven Plugin for Ceylon 1.0.0+


This plugin implements a `car` packaging type with a custom lifecycle currently including the following goals

* compile
* run
* test-compile
* test
* package
* install

The `compile`, `test-compile`, `test`, `package` and `install` goals are bound to the corresponding lifecycle phase.

The plugin requires a Ceylon SDK to be installed, it launches the `ceylon` tool from the SDK, much like the Ceylon Ant tasks do.

The Ceylon SDK home must be set as property `ceylon.home`. The environment variable `CEYLON_HOME` will be used as fallback when the property is not set.


Building the plugin
-------------------

Clone this repository and run `mvn install`.

Using the plugin
----------------

Have a look at the integration tests in `src/it/*`.

Rough outline:

* Set packaging `car`.
* Add the `ceylon-maven-plugin` and configure it.
* Have only one module per Maven artifact (and an optional test module).
* `target` is the default local module repository.
* Put any root-level resources (META-INF or WEB-INF stuff) in `src/main/resources`. The `package` goal adds these to the CAR produced by `ceylon compile`. It also adds directory entries to the archive and updates the SHA1 checksum file.
* The `install` goal copies the CAR and the POM to the local Maven repository.
