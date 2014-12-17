package com.readytalk

import com.readytalk.gradle.util.PluginUtils
import com.readytalk.gradle.util.StringUtils
import nebula.test.ProjectSpec

class PluginUtilsTest extends ProjectSpec implements PluginUtils {

  def setupPluginCondition() {
    project.ext.pluginsApplied = false
    withPlugins(['java', 'application']) {
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
    StringUtils.snakeConvert('travis_pull_request').equals('travisPullRequest')
    StringUtils.snakeConvert('travis_pull_request', true).equals('TravisPullRequest')
    StringUtils.snakeConvert('travis_pull_request', true, '-').equals('Travis-Pull-Request')
  }

  def "converts camel case to snake case"() {
    expect:
    StringUtils.camelConvert('buildJobId').equals('build_job_id')
    StringUtils.camelConvert('buildJobId', true).equals('Build_Job_Id')
    StringUtils.camelConvert('buildJobId', true, '-').equals('Build-Job-Id')
  }
}
