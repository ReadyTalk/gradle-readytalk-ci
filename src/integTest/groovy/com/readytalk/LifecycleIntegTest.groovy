package com.readytalk

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

class LifecycleIntegTest extends IntegrationSpec {
  def 'basic java project can build'() {
    writeHelloWorld('com.readytalk')
    buildFile << '''
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'java'

      buildEnv.isCI = false
    '''.stripIndent()

    expect:
    ExecutionResult result = runTasksSuccessfully('ci')
    result.wasExecuted('build')
    !result.wasExecuted('publish')
  }

  //TODO: Setup fake local repos to test simple publishing
}