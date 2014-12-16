package com.readytalk

import nebula.test.PluginProjectSpec

class IntegTestPluginTest extends PluginProjectSpec {
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
    when: project.with {
      apply plugin: 'java'
      apply plugin: 'idea'
      apply plugin: pluginName
      evaluate()
    }

    then:
    project.idea.module.scopes.TEST.plus.contains(project.configurations.integTestCompile)
  }

  def "can apply with eclipse plugin enabled"() {
    when: project.with {
      apply plugin: 'java'
      apply plugin: 'eclipse'
      apply plugin: pluginName
    }

    then:
    project.eclipse.classpath.plusConfigurations.contains(project.configurations.integTestCompile)
  }
}
