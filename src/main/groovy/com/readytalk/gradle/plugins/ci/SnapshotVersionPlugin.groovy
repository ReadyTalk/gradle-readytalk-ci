package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class SnapshotVersionPlugin implements Plugin<Project>, PluginUtils {
  Project project
  private CiInfoExtension infoExt

  void apply(final Project project) {
    this.project = project
    def infoPlugin = project.plugins.apply(CiInfoPlugin)
    this.infoExt = infoPlugin.extension

    infoExt.watchProperty('release', { baseVersion, boolean release ->
      project.version = baseVersion + (release ? '' : '-SNAPSHOT')
      project.logger.info "release status set to ${release}, updating version to ${project.version}"
    }.curry(project.version))
  }
}
