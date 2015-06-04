package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.plugins.ci.tasks.CiTask
import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

class CiLifecyclePlugin implements Plugin<Project>, PluginUtils {
  static final String CI_LIFECYCLE_TASK_NAME = 'ci'
  private CiInfoExtension infoExt
  Project project

  void apply(final Project project) {
    this.project = project
    project.plugins.with {
      def infoPlugin = apply(CiInfoPlugin)
      this.infoExt = infoPlugin.extension
      apply(CiPublishingPlugin)
    }
    setupLifecycleTask()
    applyConventions()
  }

  private void setupLifecycleTask() {
    project.with {
      tasks.create(CI_LIFECYCLE_TASK_NAME, CiTask).configure { ciTask ->
        //defaultTasks is a plain List, so we can't hook it with all{}
        afterEvaluate {
          if (tasks.findByName('build') != null) {
            dependsOn tasks.build
          }
          dependsOn tasks.matching { it.name.equals('integTest') },
              defaultTasks.findAll { !it.equals(CI_LIFECYCLE_TASK_NAME) }
        }
      }
    }
  }

  //TODO: Include more conventions and split into separate plugin class
  private void applyConventions() {
    //TODO: Move to a proper release conventions plugin instead of ad hoc
    enablePublishing()
    configureTestTasks()
    configureJacoco()
  }

  private void enablePublishing() {
    withTask(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) { publishTask ->
      project.with {
        //TODO: Use model dsl if Gradle >= 2.4
        afterEvaluate {
          if (infoExt.isCi() && (infoExt.isMasterBranch() || infoExt.isRelease() || infoExt.isReleaseBranch())) {
            tasks[CI_LIFECYCLE_TASK_NAME].dependsOn publishTask
          }
        }
      }
    }
  }

  private void configureTestTasks() {
    project.with {
      tasks.withType(Test) { Test testTask ->
        // Better Test logging
        testTask.testLogging {
          exceptionFormat = 'full'
          if (project.hasProperty('stdOut')) {
            showStandardStreams = true
          }
        }
      }
    }
  }

  private void configureJacoco() {
    project.with {
      withPlugins(['jacoco', 'java']) {
        withTask('jacocoTestReport') { JacocoReport jacocoTask ->
          tasks.check.dependsOn jacocoTask
          jacocoTask.reports.xml.enabled = true
        }
      }
    }
  }
}
