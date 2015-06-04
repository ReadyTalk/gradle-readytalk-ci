package com.readytalk.gradle.plugins.ci.version

import com.readytalk.gradle.plugins.ci.CiInfoExtension
import com.readytalk.gradle.plugins.ci.CiInfoPlugin
import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class AbstractVersionPlugin implements Plugin<Project>, PluginUtils {
  Project project
  protected CiInfoExtension infoExt
  protected String baseVersion

  void apply(Project project) {
    this.project = project
    def infoPlugin = project.plugins.apply(CiInfoPlugin)
    this.infoExt = infoPlugin.extension
    this.baseVersion = project.version

    infoExt.watchProperty('baseVersion') { String baseVersion ->
      this.baseVersion = baseVersion
    }

    if(baseVersion == 'unspecified') {
      project.logger.warn("CI Versioning plugins expect base version to be set before plugin application!")
      project.logger.warn("If required, you can override later by setting ${CiInfoPlugin.EXTENSION_NAME}.baseVersion")
    }
  }
}
