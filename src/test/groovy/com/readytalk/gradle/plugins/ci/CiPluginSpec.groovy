package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.TestUtils
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository
import org.gradle.api.publish.plugins.PublishingPlugin
import spock.lang.Unroll

class CiPluginSpec extends PluginProjectSpec implements TestUtils {
  Repository repo

  @Override
  String getPluginName() {
    return 'com.readytalk.ci'
  }

  def "base ci task dependencies wired lazily"() {
    given:
    project.with {
      apply plugin: 'java'
      defaultTasks(':clean')
      apply plugin: pluginName
      //Gradle 2.3 won't create build task until explicitly referenced
      tasks.build
    }

    when:
    project.evaluate()

    then:
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'build')
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'integTest')
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'clean')
  }
}