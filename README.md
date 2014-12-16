Gradle CI Conventions Plugin
============================

[![License](http://goo.gl/pPDj6N)](http://goo.gl/93tPwk)

Initial prototype for an opinionated conventions plugin for ReadyTalk gradle projects.

##Purpose

* Configure build and artifact metadata such as git branch automatically
* Bake build and artifact metadata into artifacts
* Add generic enhancements and conventions for all projects
* CI Lifecycle task that can intelligently figure out what to run by default
    -> e.g. automatically publish if running on CI server and the branch is master
* Standard publish task that abstracts over underlying publishing mechanism
    * Supports artifactory and bintray plugins
    * Supports direct use of ivy-publish and maven-publish plugins

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

###TODO

* Basic documentation of existing features, improved automated tests
* Example projects

### Future work

* Move more conventions into plugin
* Better extensibility - allow third parties to extend base functionality
    * May want to open pull requests with nebula plugins
        * e.g. can't extend nebula CI plugin as-is
* Better integration tests for different types of projects

###Usage

artifactoryPublish example:

```
apply plugin: 'com.readytalk.ci'
apply plugin: 'com.jfrog.artifactory'

version = version + "-${info.buildNumber}"
artifactory.publish.repository.repoKey = 'plugins-releases-local'

info {
  //Adds "BuildUser" field to manifest
  buildUser = System.getProperty("user.name")
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

 * Uses an early version of the `nebula.info` plugin to maintain Java 6 compatibility
