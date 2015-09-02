package com.readytalk.gradle.plugins.ci

import com.fizzpod.gradle.plugins.info.InfoPlugin
import com.fizzpod.gradle.plugins.info.ci.TravisProvider
import com.readytalk.gradle.util.PluginUtils
import com.readytalk.gradle.util.StringUtils
import nebula.plugin.info.InfoBrokerPlugin
import nebula.plugin.info.ci.ContinuousIntegrationInfoProvider
import nebula.plugin.info.ci.JenkinsProvider
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor

class CiInfoPlugin implements Plugin<Project>, PluginUtils {
  static final String EXTENSION_NAME = 'buildEnv'
  Project project

  //TODO: support other VCS systems, possibly by delegating to a third-party gradle plugin
  protected Repository gitRepo
  CiInfoExtension extension

  void apply(final Project project) {
    this.project = project
    this.extension = new CiInfoExtension()
    def repoSetup = new RepositoryBuilder().findGitDir(project.rootDir)
    if(repoSetup.gitDir == null) {
      project.logger.warn("No git directory directory found - this will prevent many of the CI plugin features from working.")
    } else {
      this.gitRepo = repoSetup.build()
    }

    applyNebulaInfoPlugins()
    addCiInfoExtension()
    populateCiInfo(System.getenv())
    mapInfoBrokerFields()
    configureIvyPublish()
    configureArtifactory()
  }

  private void applyNebulaInfoPlugins() {
    //com.fizzpod.gradle.plugins.info.InfoPlugin
    project.plugins.apply(InfoPlugin)
  }

  private void addCiInfoExtension() {
    project.extensions.add(EXTENSION_NAME, this.extension)
  }

  private void setDefaults(Map env) {
    extension.branch = gitRepo?.branch ?: ''
    extension.masterBranch { isMaster(branch) }
    extension.releaseBranch { isReleaseBranch(branch) }
    extension.release { isReleaseTag(branch) }
  }

  private boolean refResolvesToMaster() {
    if(gitRepo == null) return false

    def masterRef = (gitRepo.getRef('refs/heads/master') ?:
            gitRepo.getRef('refs/heads/origin/master'))?.objectId?.name
    def headRef = gitRepo.resolve('HEAD')?.name

    return (masterRef && headRef) && (masterRef == headRef)
  }

  private boolean isMaster(String branch) {
    branch ==~ /master/ || refResolvesToMaster()
  }

  private boolean isReleaseTag(String branch) {
    // TODO: Try to resolve HEAD to release tags
    branch ==~ /v\d+\.\d+\.\d+/
  }

  private boolean isReleaseBranch(String branch) {
    // TODO: Try to resolve HEAD to release branch
    branch ==~ /(origin\/)?release_\d+\.\d+\.\d+/ || branch ==~ '(origin/)?project.name-.*'
  }

  private void populateCiInfo(Map env) {
    setDefaults(env)

    ContinuousIntegrationInfoProvider ci =
            project.plugins.findPlugin('com.fizzpod.info-ci')?.selectedProvider

    switch (ci) {
      case TravisProvider:
        populateTravisInfo(env)
        break
      case JenkinsProvider:
        populateJenkinsInfo(env)
        break
    }

    extension.buildNumber = ci.calculateBuildNumber(project)

    // Enable manual override for testing, etc.
    if (env.'CI'?.toBoolean()) {
      extension.ci = true
    }
  }

  private void populateTravisInfo(Map env) {
    // Reference: http://docs.travis-ci.com/user/ci-environment/#Environment-variables
    //TODO: Option 2: info extension auto-listens to other buildEnv values?
    extension.with {
      ciProvider = 'travis'
      env.each { k, v ->
        if (k.startsWith('TRAVIS_')) {
          def prop = StringUtils.snakeConvert(k.toLowerCase())
          setProperty(prop, v)
        }
      }
      branch = env.'TRAVIS_BRANCH'
      ci = true
      masterBranch { isMaster(branch) && travisPullRequest == 'false' }
      releaseBranch { isReleaseBranch(branch) && travisPullRequest == 'false' }
    }
  }

  private void populateJenkinsInfo(Map env) {
    extension.with {
      buildHost = env.'HOSTNAME'
      branch = env.'GIT_BRANCH'
      ci = true
      ciProvider = 'jenkins'
    }
  }

  private void mapInfoBrokerFields() {
    //Map all info fields into broker plugin
    project.with {
      plugins.withId('info-broker') { InfoBrokerPlugin broker ->
        //Override nebula Build-Number, ours is more thorough
        afterEvaluate {
          def ciProps = (CiInfoExtension.getDeclaredFields().findAll {
            !it.synthetic && it.name != 'props'
          }.collectEntries { k ->
            [(k.name): extension[k.name]]
          } + extension.props).collectEntries { String k, v ->
            [(StringUtils.camelConvert(k, true, '-')): v]
          }
          //Override nebula.info values with ones from this plugin's extension if values exist in both
          def conflicts = broker.container.findAll { ciProps.containsKey(it.name) }
          broker.container.removeAll(conflicts)
          ciProps.each { String key, value ->
            broker.add(key) { value.toString() }
          }
        }
      }
    }
  }

  private void configureIvyPublish() {
    project.with {
      plugins.withId('ivy-publish') {
        //Bake metadata into ivy.xml
        tasks.withType(GenerateIvyDescriptor) {
          doFirst {
            descriptor.status = extension.buildStatus
            descriptor.branch = extension.branch
            plugins.withId('info-broker') { InfoBrokerPlugin broker ->
              descriptor.extraInfo.add('ci', 'info', broker.buildManifestString())
            }
          }
        }
      }
    }
  }

  //TODO: Split this stuff out into a broker/collector form similar to nebula.info
  private void configureArtifactory() {
    project.with {
      withAnyPlugin(['artifactory', 'com.jfrog.artifactory']) {
        extension.watchProperty('buildNumber') { String buildNumber ->
          clientConfig.info.setBuildNumber(buildNumber)
        }
        extension.watchProperty('ciProvider') { String provider ->
          def env = System.getenv()
          clientConfig.info.with {
            switch(provider) {
              case 'jenkins':
                setVcsUrl(env.'GIT_URL')
                setBuildUrl(env.'BUILD_URL')
                setBuildName(env.'JOB_NAME' ?: getBuildName())
                setPrincipal(env.'BUILD_USER_ID' ?: getPrincipal())
                break
              case 'travis':
                setVcsUrl("https://github.com/${buildEnv.travisRepoSlug}")
                setBuildUrl("https://travis-ci.org/${extension.travisRepoSlug}/builds/${extension.travisBuildId}")
                setBuildName(extension.travisRepoSlug)
                break
              default:
                setBuildUrl('')
                setVcsUrl('')
            }
            if (gitRepo != null) {
              setVcsRevision(gitRepo.resolve('HEAD')?.name ?: 'NONE')
            }
          }
        }
      }
    }
  }
}
