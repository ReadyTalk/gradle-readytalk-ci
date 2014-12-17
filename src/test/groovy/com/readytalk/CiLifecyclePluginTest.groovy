package com.readytalk

import nebula.test.PluginProjectSpec
import com.readytalk.gradle.plugins.CiLifecyclePlugin
import org.gradle.api.publish.plugins.PublishingPlugin

class CiLifecyclePluginTest extends PluginProjectSpec implements TestUtils {
  @Override
  String getPluginName() {
    return 'com.readytalk.ci'
  }

  def "base ci task dependencies wired lazily"() {
    when:
    project.apply plugin: 'java'
    project.defaultTasks(':clean')
    project.apply plugin: pluginName
    project.evaluate()

    then:
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'build')
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'integTest')
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, 'clean')
  }


  def "auto-applies integTest plugin"() {
    when:
    project.apply plugin: pluginName
    project.apply plugin: 'scala'

    then:
    project.plugins.hasPlugin('com.readytalk.integTest')
  }

  def "master branch on ci server enables publishing"() {
    when:
    project.with {
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'ivy-publish'
      info.branch = 'master'
      info.isCI = true
    }
    project.evaluate()

    then:
    project.tasks.getByName(CiLifecyclePlugin.PUBLISH_TASK)
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, CiLifecyclePlugin.PUBLISH_TASK)
  }

  def "publish task name matches gradle-defined version"() {
    expect:
    CiLifecyclePlugin.PUBLISH_TASK.equals(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
  }

  def "can apply with jacoco plugin"() {
    when:
    project.plugins.with {
      apply(this.pluginName)
      apply('jacoco')
      apply('java')
    }

    then:
    hasTaskDependency('check', 'jacocoTestReport')
  }
}