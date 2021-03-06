package com.readytalk.gradle.plugins.ci

import nebula.test.PluginProjectSpec

class BuildNumberVersionPluginSpec extends PluginProjectSpec {
  @Override
  String getPluginName() {
    return 'com.readytalk.ci.version.buildnumber'
  }

  def "appends buildnumber to version for ci builds"() {
    given:
    project.with {
      version = "1.2.3"
      apply plugin: this.pluginName
      buildEnv.ci = true
      buildEnv.buildNumber = 37
    }

    when:
    project.evaluate()

    then:
    project.version == "1.2.3-37"
  }

  def "appends '-DEV' to version for non-ci builds"() {
    given:
    project.with {
      version = "1.2.3"
      apply plugin: this.pluginName
      buildEnv.ci = false
    }

    when:
    project.evaluate()

    then:
    project.version == "1.2.3-DEV"
  }
}
