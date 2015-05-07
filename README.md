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

### Usage

#### artifactoryPublish example:

```groovy
plugins {
  id 'com.readytalk.ci' version '0.3.0'
  id 'com.jfrog.artifactory' version '3.0.3'
}

//Add custom metadata
buildEnv {
  //This would add a "Custom-Field" field to the jar manifest
  customField = System.getProperty("my.custom.property.name")
}

publishing {
  publications {
    maven(MavenPublication) {
      from components.java
    }
  }
}

//Alternatively, if you use ivy metadata:
publishing {
  publications {
    ivy(IvyPublication) {
      from components.java
    }
  }
}
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
