package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildNumberVersionPlugin implements Plugin<Project>, PluginUtils {
  Project project
  private CiInfoExtension infoExt

  void apply(final Project project) {
    this.project = project
    project.with {
      def infoPlugin = plugins.apply(CiInfoPlugin)
      this.infoExt = infoPlugin.extension

      afterEvaluate {
        if (infoExt.isCi()) {
          project.version += "-${infoExt.buildNumber}"
        } else {
          project.version += "-DEV"
        }
      }
    }
  }
}