package com.readytalk

import com.readytalk.gradle.plugins.CiLifecyclePlugin
import nebula.test.ProjectSpec
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository
import org.gradle.api.publish.plugins.PublishingPlugin

class CiPublishingTest extends ProjectSpec implements TestUtils {
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

  def "creates local install task when publish plugins applied"() {
    when:
    project.plugins.apply(CiLifecyclePlugin)
    project.plugins.apply(publishPlugin)

    then:
    project.tasks.findByName('install') != null
    project.tasks.install.group.equals(PublishingPlugin.PUBLISH_TASK_GROUP)

    where:
    publishPlugin << ['ivy-publish', 'maven-publish']
  }

  def "wires up to third party publish tasks"() {
    when:
    project.plugins.apply(CiLifecyclePlugin)
    project.plugins.apply('ivy-publish')
    project.plugins.apply(publishPlugin)

    then:
    hasTaskDependency(CiLifecyclePlugin.PUBLISH_TASK, publishTaskName)

    where:
    publishPlugin           | publishTaskName
    'com.jfrog.bintray'     | 'bintrayUpload'
    'com.jfrog.artifactory' | 'artifactoryPublish'
  }

  def "uses local ivy repo for install task"() {
    when:
    setupIvyRepo()

    then:
    project.publishing.repositories.getByName('local') instanceof IvyArtifactRepository
  }

  def "ivy publish tasks wired up for install"() {
    def taskName = 'publishFakePublicationToLocalRepository'

    when:
    setupIvyRepo()
    project.plugins.apply('java')
    project.publishing {
      publications {
        fake(IvyPublication) {
          from project.components.java
        }
      }
    }
    project.evaluate()

    then:
    project.tasks.getByName(taskName) instanceof PublishToIvyRepository
    hasTaskDependency('install', taskName)
  }
}
