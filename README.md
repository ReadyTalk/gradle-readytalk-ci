Gradle CI Conventions Plugin
============================
[![Build Status](http://goo.gl/RyKaY9)](http://goo.gl/Caq7yS)
[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)
[![Coverage](http://goo.gl/DyjVk5)](http://goo.gl/23kpJJ)

[ReadyTalk][]'s set of opinionated conventions in plugin form for internal and
external Gradle projects.

[ReadyTalk]: http://www.readytalk.com/

## Goals
Establish common conventions (particularly around continuous integration) for
Gradle projects. Takes some inspiration from the [nebula project plugin][] from
[Netflix][].

[Netflix]: https://github.com/nebula-plugins

  * Add CI lifecycle task to provide a single entry point for CI systems
  * Abstract over different publishing mechanisms to provide a single `publish`
    and `install` lifecycle tasks
  * Collect and bake in build metadata into artifacts
  * Apply generic project conventions, such as creating an integTest task and
    source set

[nebula project plugin]: https://github.com/nebula-plugins/nebula-project-plugin

## Usage
See the [ReadyTalk CI Plugin][] page on the [Gradle Plugin Portal][] for more
information on the latest published verison of the plugin and alternative methods
to apply the plugin for older Gradle versions.

[ReadyTalk CI Plugin]: https://plugins.gradle.org/plugin/com.readytalk.ci
[Gradle Plugin Portal]: https://plugins.gradle.org/

The functionality the CI plugin provides depends on the other plugins you apply
to the project. The CI plugin will detect the other plugins and configure them
appropriately.

The most basic application of the CI plugin will get you the basic lifecycle
tasks and also apply the 'base' Gradle plugin:

```groovy
plugins {
  id 'com.readytalk.ci' version '0.4.3'
}
```

For a basic `java` project published to an Artifactory repository Maven-style
(with a `pom.xml`) and SNAPSHOT versioning you will want something like this
(recommended for libraries and Gradle plugins):

```groovy
plugins {
  id 'java'
  id 'maven-publish'
  id 'com.readytalk.ci' version '0.4.3'
  id 'com.jfrog.artifactory' version '3.0.3'
}

apply plugin: 'com.readytalk.ci.version.snapshot'

publishing {
  publications {
    maven(MavenPublication) {
      from components.java
    }
  }
}
```

For a basic java project plublished to Artifactory Ivy-style (with an
`ivy.xml`) and buildnumber versioning, something like the following will be
more your speed (recommended for services and applications delivered as part of
a continuous delivery pipeline):

```groovy
plugins {
  id 'java'
  id 'ivy-publish'
  id 'com.readytalk.ci' version '0.4.3'
  id 'com.jfrog.artifactory' version '3.0.3'
}

apply plugin: 'com.readytalk.ci.version.buildnumber'

publishing {
  publications {
    ivy(IvyPublication) {
      from components.java
    }
  }
}
```

## Tasks
The ReadyTalk CI Plugin provides a few top-level "lifecycle" Gradle tasks.

  * `ci` lifecycle task is the standard entry point for CI systems
    - Default to running the build command plus `integTest`, and default tasks
  * `publish` lifecycle is standard publish task aggregator
    - Automatically depends on `artifactoryPublish`, `bintrayUpload`, or
        `publishPlugins` depending on what plugins are applied.
    - Supports the `ivy-publish`, `maven-publish`, and `plugin-publish` plugins
  * `install` will publish artifact to local filesystem if applicable
    - Projects using `maven-publish` will hook Gradle's built-in task
    - Ivy projects will look for a 'local' ivy repo to publish to
  * `integTest` is a `Test` task and will run any integration tests placed in the
    `integTest` sourceset (`src/integTest`).

## Conventions
The ReadyTalk CI plugin provides the following conventions:

  * Auto-wire jacoco reporting into check phase
  * `ci` task auto-depends on `publish` if building master, a release branch
    (i.e. `release_1.2.3`), or a release tag (i.e. `v1.2.3`) on a CI server.
    - Supports Travis CI and Jenkins
  * [Artifactory Plugin][] automatically includes publications by default
  * Build metadata placed in ivy description section
    - Also in jar manifests via the nebula.info plugin
  * Build metadata mapped from `buildEnv` extension object
    - Pre-populated with various default values from the environment
    - Can map arbitrary values (expando object)

[Artifactory Plugin]: https://www.jfrog.com/confluence/display/RTF/Gradle+Artifactory+Plugin

## Optional Versioning Plugins
There are two versioning convention plugins provided with the ReadyTalk CI
plugin, but they need to be applied separately. For both of these plugins, it
is reccommended the version be stored in `gradle.properties`, but other
mechanisms should work - the version string is modified after the build
script's configuration phase has been evaluated.

### Snapshot version plugin
This plugin provides a Maven-style versioning conventions where '-SNAPSHOT' is
appended to pre-release development build versions. With this plugin, you only
need to store the version of the next intened release in the codebase.

To apply the Snapshot Version Plugin, make sure the CI plugin is already on the
classpath (using either the "classic" way or the new `plugins{}` closure and
add:

```groovy
apply plugin: 'com.readytalk.ci.version.snapshot'
```

### Buildnumber version plugin
This plugin is intended to be used for projects using a continuous delivery
pipeline with multiple test and deployment stages. The build number from the CI
server is appended to the version string to each instance of the pipeline to
use its own artifacts.

To apply the BuildNumber Version Plugin, make sure the CI plugin is already on
the classpath (using either the "classic" way or the new `plugins{}` closure
and add:

```groovy
apply plugin: 'com.readytalk.ci.version.buildnumber'
```

### Notes

  * Uses an early version of the `nebula.info` plugin by default to maintain
    Java 6 compatibility. You can manually add the newer 2.0.0 version to your
    project if using Java 7 or later.
  * Requires Gradle 2.1 or later

### Future work

  * Move more conventions into plugin
  * Include release/versioning conventions
  * Better extensibility - allow third parties to extend base functionality
    - May want to open pull requests with nebula plugins e.g. can't extend nebula CI plugin as-is
  * Better integration tests for different types of projects
  * Example projects
  * Optional default publications
  * Optionally disable nebula jar manifest modification
    - This can cause issues with Gradle's up-to-date checks on jars

### Gradle 2.4 support
