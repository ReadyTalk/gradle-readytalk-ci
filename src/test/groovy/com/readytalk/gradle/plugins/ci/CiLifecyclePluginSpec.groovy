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
    return 'com.readytalk.ci.lifecycle'
  }

  def setup() {
    repo = createMockGitRepo()
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
    project.tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
    hasTaskDependency(CiLifecyclePlugin.CI_LIFECYCLE_TASK_NAME, PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) == publishEnabled

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