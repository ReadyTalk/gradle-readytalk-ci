package com.readytalk.gradle.plugins

import com.readytalk.gradle.util.PluginUtils
import com.readytalk.gradle.util.StringUtils
import nebula.plugin.info.InfoBrokerPlugin
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor

class InfoExtensionsPlugin implements Plugin<Project>, PluginUtils {
  static final String EXTENSION_NAME = 'buildEnv'
  Project project

  //TODO: Consider using GrGit instead of JGit
  Repository gitRepo
  InfoExtension extension

  void apply(final Project project) {
    this.project = project
    gitRepo = new RepositoryBuilder().findGitDir(project.rootDir).build()

    project.with {
      this.extension = new InfoExtension()
      project.extensions.add(InfoExtensionsPlugin.EXTENSION_NAME, this.extension)
      setDefaults()
      populateCIInfo()

      //Map all info fields into broker plugin
      plugins.withId('info-broker') { InfoBrokerPlugin broker ->
        //Override nebula Build-Number, ours is more thorough
        afterEvaluate {
          def buildNumber = broker.container.find { it.name == 'Build-Number' }
          if(buildNumber) broker.container.remove(buildNumber)
          (InfoExtension.getDeclaredFields().findAll {
            !it.synthetic && it.name != 'props'
          }.collectEntries { k ->
            [ (k.name):extension[k.name] ]
          } + extension.props).each { k, v ->
            String key = StringUtils.camelConvert(k,true,'-')
            broker.add(key) { v.toString() }
          }
        }
      }

      plugins.withId('ivy-publish') {
        //Bake metadata into ivy.xml
        tasks.withType(GenerateIvyDescriptor) {
          doFirst {
            descriptor.status = extension.buildStatus
            descriptor.branch = extension.branch
            plugins.withId('info-broker') { InfoBrokerPlugin broker ->
              descriptor.extraInfo.add('ci','info',broker.buildManifestString())
            }
          }
        }
      }

      withAnyPlugin(['artifactory', 'com.jfrog.artifactory']) {
        afterEvaluate {
          clientConfig.info.setBuildNumber(extension.buildNumber)
        }
      }
    }
  }

  void setDefaults() {
    extension.branch = gitRepo.branch ?: ''
    extension.buildStatus = 'integration'
    extension.ciProvider = 'none'
    extension.isCI = false
    extension.buildNumber = project.plugins.findPlugin('info-ci')?.extension?.buildNumber ?: 'local'
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
            def prop = StringUtils.snakeConvert(k.toLowerCase())
            setProperty(prop, v)
          }
        }
      }

      //Override
      if(System.getenv('CI')?.toBoolean()) {
        isCI = true
      }
    }
  }
}
