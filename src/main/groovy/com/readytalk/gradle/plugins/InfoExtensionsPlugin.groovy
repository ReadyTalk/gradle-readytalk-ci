package com.readytalk.gradle.plugins

import com.readytalk.gradle.util.PluginUtils
import nebula.plugin.info.InfoBrokerPlugin
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor

class InfoExtensionsPlugin implements Plugin<Project> {
  static final String EXTENSION = 'info'
  private Project project

  //TODO: Consider using GrGit instead of JGit
  Repository gitRepo
  InfoExtension extension

  void apply(final Project project) {
    this.project = project
    gitRepo = new RepositoryBuilder().findGitDir(project.rootDir).build()

    project.with {
      this.extension = project.extensions.create(InfoExtensionsPlugin.EXTENSION, InfoExtension)
      setDefaults()
      populateCIInfo()

      //Map all info fields into broker plugin if present
      plugins.withId('info-broker') { InfoBrokerPlugin broker ->
        afterEvaluate {
          extension.properties.each { k, v ->
            broker.add(k.capitalize()) { v.toString() }
          }
        }
      }

      plugins.withId('ivy-publish') {
        //Bake metadata into ivy.xml
        tasks.withType(GenerateIvyDescriptor) {
          doFirst {
            descriptor.status = extension.buildStatus
            descriptor.branch = extension.branch
            descriptor.withXml {
              plugins.withId('info-broker') { InfoBrokerPlugin broker ->
                //TODO: Probably a cleaner way to access this
                asNode().info[0].appendNode('description', broker.buildManifestString())
              }
            }
          }
        }
      }

      PluginUtils.withAnyPlugin(project, ['artifactory', 'com.jfrog.artifactory']) {
        afterEvaluate {
          clientConfig.info.setBuildNumber(extension.buildNumber)
        }
      }
    }
  }

  void setDefaults() {
    extension.branch = gitRepo.branch
    extension.buildStatus = 'integration'
    extension.ciProvider = 'none'
    extension.buildNumber = 'local'
    extension.isCI = false
  }

  void populateCIInfo() {
    extension.with {
      //Jenkins CI
      if(System.getenv('BUILD_NUMBER') && System.getenv('JOB_NAME')) {
        ciProvider = 'jenkins'
        buildNumber = System.getenv('BUILD_NUMBER')
        buildId = System.getenv('BUILD_ID')
        branch = System.getenv('GIT_BRANCH')
        isCI = true
      }

      //Travis CI
      //Reference: http://docs.travis-ci.com/user/ci-environment/#Environment-variables
      if(System.getenv('TRAVIS')) {
        ciProvider = 'travis'
        buildNumber = System.getenv('TRAVIS_BUILD_NUMBER')
        branch = System.getenv('TRAVIS_BRANCH')
        isCI = true
        System.getenv().each { k, v ->
          if(k.startsWith('TRAVIS_')) {
            def prop = PluginUtils.snakeConvert(k.toLowerCase())
            setProperty(prop, v)
          }
        }
      }

      //Override
      if(System.getenv('CI')) {
        isCI = true
      }
    }
  }
}
