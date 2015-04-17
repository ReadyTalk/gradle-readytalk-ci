Gradle CI Conventions Plugin
============================

[![Build Status](http://goo.gl/RyKaY9)](http://goo.gl/Caq7yS)
[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)
[![Coverage](http://goo.gl/DyjVk5)](http://goo.gl/23kpJJ)

[ReadyTalk][]'s set of opinionated conventions in plugin form for internal and external Gradle projects.

[ReadyTalk]: http://www.readytalk.com/

##Goals
Establish common conventions (particularly around continuous integration) for Gradle projects. Takes some inspiration from the [nebula project plugin][] from [Netflix][].

[Netflix]: https://github.com/nebula-plugins

 * Add CI lifecycle task to provide a single entry point for CI systems
 * Abstract over different publishing mechanisms to provide a single `publish` and `install` lifecycle tasks
 * Collect and bake in build metadata into artifacts
 * Apply generic project conventions, such as creating an integTest task and source set

[nebula project plugin]: https://github.com/nebula-plugins/nebula-project-plugin

##Tasks
* `ci` lifecycle task is the standard entry point for CI systems
    * Default to running the build command plus integTest, and default tasks

* `publish` lifecycle is standard publish task aggregator
    * Automatically depends on artifactoryPublish or bintrayUpload if applicable
    * Supports the ivy-publish / maven-publish plugins

* `install` will publish artifact to local filesystem if applicable
    * Projects using `maven-publish` will hook Gradle's built-in task
    * Ivy projects will look for a 'local' ivy repo to publish to

##Conventions

 * Adds integTest task for integration tests
 * Auto-wire jacoco reporting into check phase
 * `ci` task auto-depends on `publish` if building master in a CI build
     * Supports Travis CI and Jenkins
 * [Artifactory Plugin][] automatically includes publications by default
 * Build metadata placed in ivy description section
     * Also in jar manifests via the nebula.info plugin
 * Build metadata mapped from `buildEnv` extension object
     * Pre-populated with various default values from the environment
     * Can map arbitrary values (expando object)

[Artifactory Plugin]: https://www.jfrog.com/confluence/display/RTF/Gradle+Artifactory+Plugin

###Usage

####artifactoryPublish example:

```
plugins {
  id 'com.readytalk.ci' version '0.3.0'
  id 'com.jfrog.artifactory' version '3.0.3'
}

//Appending a build number
version = version + "-${buildEnv.buildNumber}"

//Add custom metadata
buildEnv {
  //This would add a "Custom-Field" field to the jar manifest
  customField = System.getProperty("my.custom.property.name")
}

//This would disable artifactoryPublish unless on a release branch
artifactoryPublish.onlyIf {
  buildEnv.branch.startsWith 'release_'
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

###Notes

 * Uses an early version of the `nebula.info` plugin by default to maintain Java 6 compatibility. You can manually add the newer 2.0.0 version to your project if using Java 7 or later.
 * Requires Gradle 2.1 or later

### Future work

* Move more conventions into plugin
* Include release/versioning conventions
* Better extensibility - allow third parties to extend base functionality
    * May want to open pull requests with nebula plugins
        * e.g. can't extend nebula CI plugin as-is
* Better integration tests for different types of projects
* Example projects
* Optional default publications
* Optionally disable nebula jar manifest modification
  - This can cause issues with Gradle's up-to-date checks on jars

### Gradle 2.4 support
