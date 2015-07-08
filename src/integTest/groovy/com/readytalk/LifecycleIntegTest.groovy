package com.readytalk

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll
import org.gradle.api.artifacts.repositories.IvyArtifactRepository

class LifecycleIntegTest extends IntegrationSpec {
  def 'basic java project can build'() {
    writeHelloWorld('com.readytalk')
    buildFile << '''
      apply plugin: 'com.readytalk.ci'
      apply plugin: 'java'

      buildEnv.ci = false
    '''.stripIndent()

    expect:
    ExecutionResult result = runTasksSuccessfully('ci')
    result.wasExecuted('build')
    !result.wasExecuted('publish')
  }

  @Unroll
  def "wires up publication tasks to #publishTaskName when using #publishPlugin"() {
    when:
    writeHelloWorld('com.readytalk')
    buildFile << """
      plugins.apply 'com.readytalk.ci'
      plugins.apply 'maven-publish'
      plugins.apply '${publishPlugin}'

      tasks.create('fauxArtifact', Zip) {
        from file('build.gradle')
      }

      publishing.publications.create('maven', MavenPublication) {
        artifact(tasks.fauxArtifact)
      }

      tasks.'${publishTaskName}' {
        onlyIf = { false }
      }
    """

    then:
    ExecutionResult result = runTasksSuccessfully(publishTaskName)
    result.wasExecuted('fauxArtifact')
    result.wasExecuted('generateMavenPomFileForMavenPublication')

    where:
    publishPlugin               | publishTaskName
    'com.jfrog.bintray'         | 'bintrayUpload'
    'com.jfrog.artifactory'     | 'artifactoryPublish'
  }

  //TODO: Setup fake local repos to test simple publishing
}
