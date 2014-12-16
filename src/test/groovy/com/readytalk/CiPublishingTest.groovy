package com.readytalk

import com.readytalk.gradle.plugins.CiLifecyclePlugin
import nebula.test.ProjectSpec
import org.gradle.api.publish.plugins.PublishingPlugin
import org.jfrog.gradle.plugin.artifactory.extractor.GradleBuildInfoExtractor

class CiPublishingTest extends ProjectSpec implements TestUtils {
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
    hasTaskDependency(CiLifecyclePlugin.PUBLISH_TASK_NAME, publishTaskName)

    where:
    publishPlugin           | publishTaskName
    'com.jfrog.bintray'     | 'bintrayUpload'
    'com.jfrog.artifactory' | 'artifactoryPublish'
  }
}
