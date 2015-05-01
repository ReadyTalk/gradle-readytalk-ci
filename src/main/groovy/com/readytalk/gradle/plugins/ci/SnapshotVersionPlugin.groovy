package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class SnapshotVersionPlugin implements Plugin<Project>, PluginUtils {
  Project project
  private CiInfoExtension infoExt

  void apply(final Project project) {
    this.project = project
    project.with {
      plugins.apply(CiInfoPlugin)
      this.infoExt = project.plugins.getPlugin(CiInfoPlugin).extension

      afterEvaluate {
        if (!infoExt.isRelease()) {
          project.version += "-SNAPSHOT"
        }
      }
    }
  }
}
