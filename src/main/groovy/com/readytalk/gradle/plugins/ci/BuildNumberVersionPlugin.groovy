package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildNumberVersionPlugin implements Plugin<Project>, PluginUtils {
  Project project
  private CiInfoExtension infoExt

  void apply(final Project project) {
    this.project = project
    def infoPlugin = project.plugins.apply(CiInfoPlugin)
    this.infoExt = infoPlugin.extension

    infoExt.watchProperties(['ci', 'buildNumber'] as Set<String>, { baseVersion, ci, buildNumber ->
      project.version = baseVersion + (ci ? "-${buildNumber}" : '-DEV')
      project.logger.info "CI status (${ci}) or buildNumber (${buildNumber}) changed, updating version string to ${project.version}"
    }.curry(project.version))
  }
}
