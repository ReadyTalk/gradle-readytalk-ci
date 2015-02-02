package com.readytalk

import nebula.test.PluginProjectSpec
import com.readytalk.gradle.plugins.CiLifecyclePlugin
import org.eclipse.jgit.lib.Repository
import org.gradle.api.publish.plugins.PublishingPlugin

class CiLifecyclePluginTest extends PluginProjectSpec implements TestUtils {
  Repository repo

  @Override
  String getPluginName() {
    return 'com.readytalk.ci'
  }

  def setup() {
    repo = createMockGitRepo()
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

  def "does enable publishing on master branch ci build"() {
    when:
    project.with {
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'ivy-publish'
      buildEnv.isCI = 'true'
    }
    project.evaluate()

    then:
    project.tasks.getByName(CiLifecyclePlugin.PUBLISH_TASK)
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, CiLifecyclePlugin.PUBLISH_TASK)
  }

  def "doesn't enable publishing on travis pull request"() {
    when:
    project.with {
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'ivy-publish'
      buildEnv.branch = 'master'
      buildEnv.isCI = 'true'
      buildEnv.ciProvider = 'travis'
      buildEnv.travisPullRequest = '45'
    }
    project.evaluate()

    then:
    !hasTaskDependency(CiLifecyclePlugin.CI_TASK, CiLifecyclePlugin.PUBLISH_TASK)
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