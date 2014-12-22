Gradle CI Conventions Plugin
============================

[![Build Status](http://goo.gl/RyKaY9)](http://goo.gl/Caq7yS)
[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)
[![Coverage](http://goo.gl/DyjVk5)](http://goo.gl/23kpJJ)

Initial prototype for an opinionated conventions plugin for ReadyTalk gradle projects.

##Goals
Establish common conventions (particularly around continuous integration) for Gradle projects. Takes some inspiration from the [nebula project plugin][].

 * Add CI lifecycle task to provide a single entry point for CI systems
 * Abstract over different publishing mechanisms to provide a single `publish` and `install` lifecycle tasks
 * Collect and bake in build metadata into artifacts
 * Apply generic project conventions, such as creating an integTest task and source set

[nebula project plugin]: https://github.com/nebula-plugins/nebula-project-plugin

##Tasks
* `ci` task is the standard entry point for CI systems
    * Default to running the build command plus integTest, and default tasks

* `publish` is standard publish task aggregator
    * Automatically depends on artifactoryPublish or bintrayUpload if applicable

* `install` will publish artifact to local filesystem if applicapble
    * Projects using `maven-publish` will hook Gradle's built-in task
    * Ivy projects will look for a 'local' ivy repo to publish to

##Conventions

 * Auto-wire jacoco reporting into check phase
 * `ci` task auto-depends on `publish` if building master in a CI build
 * Artifactory plugin automatically includes publications by default
 * Build metadata placed in ivy file description node
 * Various build metadata added to jar manifest (via nebula.info plugin)
 * Arbitrary metadata can be mapped via the info extension object

###Usage

####artifactoryPublish example:

```
plugins {
  id 'com.readytalk.ci' version '0.1.0'
  id 'com.jfrog.artifactory' version '3.0.1'
}

//Appending a build number
version = version + "-${info.buildNumber}"

info {
  //This would add a "BuildUser" field to the jar manifest
  buildUser = System.getProperty("user.name")
}

//This would disable artifactoryPublish unless on a release branch
artifactoryPublish.onlyIf {
  info.branch.startsWith 'release_'
}

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

### Future work

* Move more conventions into plugin
* Better extensibility - allow third parties to extend base functionality
    * May want to open pull requests with nebula plugins
        * e.g. can't extend nebula CI plugin as-is
* Better integration tests for different types of projects
* Example projects
