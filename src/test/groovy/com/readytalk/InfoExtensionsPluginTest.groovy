package com.readytalk

import com.readytalk.gradle.plugins.CiLifecyclePlugin
import nebula.plugin.info.InfoPlugin
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import spock.lang.Ignore

class InfoExtensionsPluginTest extends PluginProjectSpec implements TestUtils {
  Repository repo

  @Override
  String getPluginName() {
    return 'com.readytalk.info-extensions'
  }

  def setup() {
    repo = createMockGitRepo()
  }

  def "maps arbitrary properties into broker plugin"() {
    when:
    project.with {
      apply plugin: pluginName
      apply plugin: InfoPlugin
      buildEnv.pigs = "flying"
      evaluate()
    }
    def manifestMap = project.plugins.getPlugin('info-broker').buildManifest()

    then:
    manifestMap.containsKey('Pigs')
    manifestMap['Pigs'].equals('flying')
  }

  def "adds branch name from git"() {
    when:
    project.apply plugin: CiLifecyclePlugin
    project.evaluate()
    def manifestMap = project.plugins.getPlugin('info-broker').buildManifest()

    then:
    manifestMap.containsKey('Branch')
    manifestMap['Branch'].equals(repo.branch)
  }
}
