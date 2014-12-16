package com.readytalk

import com.readytalk.gradle.plugins.CiLifecyclePlugin
import nebula.plugin.info.InfoPlugin
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.RepositoryBuilder

class InfoExtensionsPluginTest extends PluginProjectSpec {
  @Override
  String getPluginName() {
    return 'com.readytalk.info-extensions'
  }

  def "maps arbitrary properties into broker plugin"() {
    when:
    project.with {
      apply plugin: pluginName
      apply plugin: InfoPlugin
      info.pigs = "flying"
      evaluate()
    }
    def manifestMap = project.plugins.getPlugin('info-broker').buildManifest()

    then:
    manifestMap.containsKey('Pigs')
    manifestMap['Pigs'].equals('flying')
  }

  def "adds branch name from git"() {
    when:
    //NOTE: Will use real project's git repo so only perform read operations
    def repo = new RepositoryBuilder().findGitDir(projectDir).build()
    project.apply plugin: CiLifecyclePlugin
    project.evaluate()
    def manifestMap = project.plugins.getPlugin('info-broker').buildManifest()

    then:
    manifestMap.containsKey('Branch')
    manifestMap['Branch'].equals(repo.branch)
  }
}
