package com.readytalk.gradle.plugins.integtest

import nebula.test.PluginProjectSpec
import spock.lang.Unroll

class IntegTestPluginSpec extends PluginProjectSpec {
  @Override
  String getPluginName() {
    return 'com.readytalk.integTest'
  }

  def "can apply plugin on java project and get integTest objects"() {
    when:
    project.with {
      apply plugin: 'java'
      apply plugin: pluginName
    }

    then:
    project.tasks.findByName('integTest')
    project.sourceSets.findByName('integTest')
    project.tasks.findByName('compileIntegTestJava')
  }

  def "can apply with idea plugin enabled"() {
    given:
    project.with {
      apply plugin: 'java'
      apply plugin: 'idea'
      apply plugin: pluginName
    }

    when:
    project.evaluate()

    then:
    project.idea.module.scopes.TEST.plus.contains(project.configurations.integTestCompile)
  }

  def "can apply with eclipse plugin enabled"() {
    when:
    project.with {
      apply plugin: 'java'
      apply plugin: 'eclipse'
      apply plugin: pluginName
    }

    then:
    project.eclipse.classpath.plusConfigurations.contains(project.configurations.integTestCompile)
  }

  @Unroll
  def "auto-applies integTest plugin when #langPlugin plugin is applied"() {
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

}
