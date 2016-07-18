package com.readytalk.gradle.plugins.ci

import com.fizzpod.gradle.plugins.info.ci.ContinuousIntegrationInfoProviderResolver
import com.fizzpod.gradle.plugins.info.ci.TravisProvider
import com.readytalk.gradle.TestUtils
import nebula.plugin.info.ci.AbstractContinuousIntegrationProvider
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository
import spock.lang.Shared
import spock.lang.Unroll

class CiInfoPluginSpec extends PluginProjectSpec implements TestUtils {
  Repository repo

  @Shared
  Map devEnv = [:]

  @Shared
  travisEnv = [
      'TRAVIS'             : 'true',
      'TRAVIS_JOB_NUMBER'  : '5678',
      'TRAVIS_BRANCH'      : 'travisMaster',
      'TRAVIS_PULL_REQUEST': '',
  ]

  @Shared
  Map jenkinsEnv = [
      'BUILD_NUMBER': '1234',
      'BUILD_ID'    : '',
      'GIT_BRANCH'  : 'jenkinsMaster',
      'JOB_NAME'  : 'test-build',
  ]

  @Override
  String getPluginName() {
    return 'com.readytalk.ci.info'
  }

  def setup() {
    repo = createMockGitRepo()
  }

  def "maps arbitrary properties into broker plugin"() {
    given:
    project.with {
      apply plugin: pluginName
      buildEnv.pigs = "flying"
    }

    when:
    project.evaluate()
    def manifestMap = project.plugins.getPlugin('nebula.info-broker').buildManifest()

    then:
    project.buildEnv.pigs == 'flying'
    manifestMap.containsKey('Pigs')
    manifestMap['Pigs'].equals('flying')
  }

  //TODO: environment variables from plugin build leak into test cases
  def "adds branch name from git"() {
    given:
    project.apply plugin: CiLifecyclePlugin
    project.buildEnv.branch = 'master'

    when:
    project.evaluate()
    def manifestMap = project.plugins.getPlugin('nebula.info-broker').buildManifest()

    then:
    manifestMap.containsKey('Branch')
    manifestMap['Branch'].equals(repo.branch)
  }

  //TODO: No longer possible to test fizzpod's extensions
  //      Groovy can't override private methods via metaclass AFAICT
  @Unroll
  def "populates info extension from #envMap"() {
    given:
    //Override environment in upstream plugins
    Closure envOverride = { String key ->
      envMap.get(key)
    }
    AbstractContinuousIntegrationProvider.metaClass.static.getEnvironmentVariable = envOverride
//    ContinuousIntegrationInfoProviderResolver.DEFAULT_PROVIDERS.each { provider ->
//      provider.metaClass.static.getEnvironmentVariable = envOverride
//    }

    def infoPlugin = new CiInfoPlugin()
    infoPlugin.setExtension(new CiInfoExtension())
    infoPlugin.gitRepo = repo
    infoPlugin.setProject(project)
    infoPlugin.setDefaults()

    when:
    project.plugins.apply('com.fizzpod.info-ci') //Required to guess ci type
    infoPlugin.populateCiInfo(envMap)

    then:
    infoPlugin.extension.ciProvider == ciProvider
    infoPlugin.extension.branch == branch
    infoPlugin.extension.buildNumber == buildNumber

    where:
    envMap     | ciProvider | branch          | buildNumber
    devEnv     | "none"     | 'master'        | 'LOCAL'
    jenkinsEnv | "jenkins"  | 'jenkinsMaster' | '1234'
//    travisEnv  | "travis"   | 'travisMaster'  | '5678'
  }
}
