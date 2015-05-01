package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.TestUtils
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository
import org.gradle.api.publish.plugins.PublishingPlugin
import spock.lang.Unroll

class CiLifecyclePluginSpec extends PluginProjectSpec implements TestUtils {
  Repository repo

  @Override
  String getPluginName() {
    return 'com.readytalk.ci'
  }

  def setup() {
    repo = createMockGitRepo()
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


  @Unroll
  def "auto-applies integTest plugin when #plugin plugin is applied"() {
    when:
    project.with {
      apply plugin: pluginName
      apply plugin: langPlugin
    }

    then:
    project.plugins.hasPlugin('com.readytalk.integTest')

    where:
    langPlugin << ['scala', 'java', 'groovy']
  }

  @Unroll("where release: #isRelease, master: #isMaster, ci: #isCi")
  def "enables publishing on ci build"() {
    given:
    project.with {
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'ivy-publish'
      buildEnv.release = isRelease
      buildEnv.masterBranch = isMasterBranch
      buildEnv.releaseBranch = isReleaseBranch
      buildEnv.ci = isCi
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName(CiLifecyclePlugin.PUBLISH_TASK)
    hasTaskDependency(CiLifecyclePlugin.CI_TASK, CiLifecyclePlugin.PUBLISH_TASK) == publishEnabled

    where:
    isRelease | isReleaseBranch | isMasterBranch | isCi  || publishEnabled
    true      | false           | false          | true  || true
    false     | true            | false          | true  || true
    false     | false           | true           | true  || true
    false     | false           | false          | true  || false
    true      | false           | false          | false || false
    false     | true            | false          | false || false
    false     | false           | true           | false || false
    false     | false           | false          | false || false
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