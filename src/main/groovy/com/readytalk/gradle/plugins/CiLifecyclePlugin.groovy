package com.readytalk.gradle.plugins

import com.readytalk.gradle.tasks.CiTask
import com.readytalk.gradle.util.PluginUtils
import nebula.plugin.info.InfoBrokerPlugin
import nebula.plugin.info.basic.BasicInfoPlugin
import nebula.plugin.info.java.InfoJavaPlugin
import nebula.plugin.info.reporting.InfoJarManifestPlugin
import nebula.plugin.info.reporting.InfoJarPropertiesFilePlugin
import nebula.plugin.info.reporting.InfoPropertiesFilePlugin
import nebula.plugin.info.scm.ScmInfoPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.build.BuildTypes
import org.gradle.testing.jacoco.tasks.JacocoReport

class CiLifecyclePlugin implements Plugin<Project>, PluginUtils {
  static final String CI_TASK = 'ci'
  static final String PUBLISH_TASK = 'publish'
  private InfoExtension info
  Project project

  void apply(final Project project) {
    this.project = project
    applyPlugins()
    setupLifecycleTask()
    applyConventions()
  }

  void applyPlugins() {
    project.plugins.with {
      //Enables a multitude of reporting plugins, safe to always enable
      //Applied piecemeal because the CI collector plugin is too simple
      apply(InfoBrokerPlugin)
      apply(BasicInfoPlugin)
      apply(ScmInfoPlugin)
      apply(InfoJavaPlugin)
      apply(InfoPropertiesFilePlugin)
      apply(InfoJarPropertiesFilePlugin)
      apply(InfoJarManifestPlugin)

      //Aggregate plugins
      apply(BasePlugin)
      apply(InfoExtensionsPlugin)
      apply(IntegTestPlugin)
      apply(CiPublishingPlugin)
    }

    this.info = project.plugins.getPlugin(InfoExtensionsPlugin).extension

    //TODO: Experimental to see if it proves useful
    project.extensions.buildTypes = new BuildTypes(project)
  }

  void setupLifecycleTask() {
    project.with {
      tasks.create(CI_TASK, CiTask).configure { ciTask ->
        //defaultTasks is a plain List, so we can't hook it with all{}
        afterEvaluate {
          dependsOn tasks.matching { it.name.equals('build') },
                  tasks.matching { it.name.equals('integTest') },
                  defaultTasks.findAll { !it.equals(CI_TASK) }
        }
      }
    }
  }

  //TODO: Include more conventions and split into separate plugin class
  void applyConventions() {
    project.with {
      //Allow overriding before taking action
      //TODO: Don't use afterEvaluate directly to simplify testing
      afterEvaluate {
        if (info.branch.equals('master') && info.isCI.toBoolean()) {
          enablePublishing()
        }
      }

      tasks.withType(Test) { Test testTask ->
        testTask.testLogging {
          exceptionFormat = 'full'
          if(project.hasProperty('stdOut')) {
            showStandardStreams = true
          }
        }
      }

      withPlugins(['jacoco', 'java']) {
        withTask('jacocoTestReport') { JacocoReport jacocoTask ->
          tasks.check.dependsOn jacocoTask
          jacocoTask.reports.xml.enabled = true
        }
      }
    }
  }

  //Actually enable publishing to happen
  void enablePublishing() {
    withTask(PUBLISH_TASK) { publishTask ->
      project.tasks[CI_TASK].dependsOn publishTask
    }
  }
}
