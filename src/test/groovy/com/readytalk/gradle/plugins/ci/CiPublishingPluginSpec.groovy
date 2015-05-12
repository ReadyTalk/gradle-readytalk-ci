package com.readytalk.gradle.plugins.ci

import com.gradle.publish.PublishPlugin
import com.readytalk.gradle.TestUtils
import nebula.test.ProjectSpec
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import spock.lang.Ignore
import spock.lang.Unroll

class CiPublishingPluginSpec extends ProjectSpec implements TestUtils {
  def setupIvyRepo() {
    project.with {
      plugins.apply(CiLifecyclePlugin)
      plugins.apply(IvyPublishPlugin)

      repositories {
        ivy {
          name 'local'
          url '.'
        }
      }
    }
  }

  @Unroll
  def "creates local install task when #publishPlugin plugin applied"() {
    when:
    project.plugins.apply(CiLifecyclePlugin)
    project.plugins.apply(publishPlugin)

    then:
    project.tasks.findByName('install') != null
    project.tasks.install.group.equals(PublishingPlugin.PUBLISH_TASK_GROUP)

    where:
    publishPlugin << ['ivy-publish', 'maven-publish']
  }

  @Unroll
  def "wires up #publishTaskName from #publishPlugin to publish task"() {
    when:
    project.plugins.apply(CiLifecyclePlugin)
    project.plugins.apply('ivy-publish')
    project.plugins.apply(publishPlugin)

    then:
    hasTaskDependency(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME, publishTaskName)

    where:
    publishPlugin               | publishTaskName
    'com.jfrog.bintray'         | 'bintrayUpload'
    'com.jfrog.artifactory'     | 'artifactoryPublish'
    'com.gradle.plugin-publish' | 'publishPlugins'
  }

  def "uses local ivy repo for install task"() {
    when:
    setupIvyRepo()

    then:
    project.publishing.repositories.getByName('local') instanceof IvyArtifactRepository
  }

  def "applies maven-publish when plugin-publish is applied"() {
    given:
    project.with{
      plugins.apply "com.gradle.plugin-publish"
      plugins.apply CiPublishingPlugin
    }

    when:
    project.evaluate()

    then:
    project.pluginManager.hasPlugin('maven-publish')
  }

  @Ignore('must be rewritten as integration test for Gradle 2.4+')
  def "ivy publish tasks wired up for install"() {
    def taskName = 'publishFakePublicationToLocalRepository'

    given:
    setupIvyRepo()
    project.plugins.apply('java')
    project.publishing {
      publications {
        fake(IvyPublication) {
          from project.components.java
        }
      }
    }

    when:
    project.evaluate()

    then:
    project.tasks.getByName(taskName) instanceof PublishToIvyRepository
    hasTaskDependency('install', taskName)
  }
}
