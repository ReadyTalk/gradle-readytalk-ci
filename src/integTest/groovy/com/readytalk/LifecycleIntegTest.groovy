package com.readytalk

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

class LifecycleIntegTest extends IntegrationSpec {
  def 'basic java project can build'() {
    writeHelloWorld('com.readytalk')
    buildFile << '''
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'java'
    '''.stripIndent()

    expect:
    ExecutionResult result = runTasksSuccessfully('build')
  }

  //TODO: Setup fake local repos to test simple publishing
}