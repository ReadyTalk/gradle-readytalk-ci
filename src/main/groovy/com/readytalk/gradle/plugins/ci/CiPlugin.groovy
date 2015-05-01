package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.plugins.integtest.IntegTestPlugin
import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class CiPlugin implements Plugin<Project>, PluginUtils {
  Project project

  void apply(final Project project) {
    this.project = project
    applyPlugins()
  }

  /**
   * Aggregate plugins
   */
  void applyPlugins() {
    project.plugins.with {
      apply(BasePlugin)
      apply(CiInfoPlugin)
      apply(CiLifecyclePlugin)
      apply(CiPublishingPlugin)
      apply(IntegTestPlugin)
    }
  }
}
