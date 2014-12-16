package com.readytalk

import com.readytalk.gradle.util.PluginUtils
import nebula.test.ProjectSpec

class PluginUtilsTest extends ProjectSpec {
  def setupPluginCondition() {
    project.ext.pluginsApplied = false
    PluginUtils.withPlugins(project, ['java', 'application']) {
      project.plugins.with {
        project.ext.pluginsApplied = (hasPlugin('java') && hasPlugin('application'))
      }
    }
  }

  def "applies withAll logic after all plugins"() {
    when:
    setupPluginCondition()
    project.apply plugin: 'java'
    project.apply plugin: 'application'

    then:
    project.ext.pluginsApplied.toBoolean()
  }

  def "doesn't apply logic if only some plugins applied"() {
    when:
    setupPluginCondition()
    project.apply plugin: 'java'

    then:
    !(project.ext.pluginsApplied.toBoolean())
  }

  def "applies logic when any plugin applied"() {
    when:
    setupPluginCondition()
    project.apply plugin: 'application'

    then:
    project.ext.pluginsApplied.toBoolean()
  }

  def "converts snake case to camel case"() {
    expect:
    PluginUtils.snakeConvert('travis_pull_request').equals('travisPullRequest')
    PluginUtils.snakeConvert('travis_pull_request', true).equals('TravisPullRequest')
    PluginUtils.snakeConvert('travis_pull_request', true, '-').equals('Travis-Pull-Request')
  }

  def "converts camel case to snake case"() {
    expect:
    PluginUtils.camelConvert('buildJobId').equals('build_job_id')
    PluginUtils.camelConvert('buildJobId', true).equals('Build_Job_Id')
    PluginUtils.camelConvert('buildJobId', true, '-').equals('Build-Job-Id')
  }
}
