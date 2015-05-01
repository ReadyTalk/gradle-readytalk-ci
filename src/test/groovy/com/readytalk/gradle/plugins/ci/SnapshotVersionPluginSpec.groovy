package com.readytalk.gradle.plugins.ci

import nebula.test.PluginProjectSpec
import spock.lang.Unroll

class SnapshotVersionPluginSpec extends PluginProjectSpec {
  @Override
  String getPluginName() {
    return 'com.readytalk.ci.version.snapshot'
  }

  @Unroll
  def "appends '-SNAPSHOT' to version for non-release builds (ci = #ci)"() {
    given:
    project.with {
      apply plugin: this.pluginName
      version = "1.2.3"
      buildEnv.ci = ci
      buildEnv.release = false
    }

    when:
    project.evaluate()

    then:
    project.version == "1.2.3-SNAPSHOT"

    where:
    ci << [true, false]
  }

  def "doesn't append anything for release builds"() {
    given:
    project.with {
      apply plugin: this.pluginName
      version = "1.2.3"
      buildEnv.ci = true
      buildEnv.release = true
    }

    when:
    project.evaluate()

    then:
    project.version == "1.2.3"
  }
}
