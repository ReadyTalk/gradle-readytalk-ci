package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.TestUtils
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository

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
    hasTaskDependency(CiLifecyclePlugin.CI_LIFECYCLE_TASK_NAME, 'build')
    hasTaskDependency(CiLifecyclePlugin.CI_LIFECYCLE_TASK_NAME, 'integTest')
    hasTaskDependency(CiLifecyclePlugin.CI_LIFECYCLE_TASK_NAME, 'clean')
  }
}