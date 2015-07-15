package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.TestUtils
import nebula.test.PluginProjectSpec
import org.eclipse.jgit.lib.Repository
import org.gradle.api.publish.plugins.PublishingPlugin
import spock.lang.Unroll

class CiLifecyclePluginSpec extends PluginProjectSpec implements TestUtils {
  @Override
  String getPluginName() {
    return 'com.readytalk.ci.lifecycle'
  }

  @Unroll("where release: #isRelease, master: #isMaster, ci: #isCi")
  def "enables publishing on ci build"() {
    given:
    createMockGitRepo()
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
    setup:
    createMockGitRepo()

    when:
    project.plugins.with {
      apply(this.pluginName)
      apply('jacoco')
      apply('java')
    }

    then:
    hasTaskDependency('check', 'jacocoTestReport')
  }

  //Warnings are okay, but don't break build setup just because .git isn't present
  def "can apply without git directory"() {
    setup:
    project.file('.git').deleteDir()

    when:
    project.plugins.with {
      apply(this.pluginName)
      apply(CiInfoPlugin)
    }

    then:
    noExceptionThrown()
  }
}