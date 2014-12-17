package com.readytalk.gradle.util

import org.gradle.api.Project
import org.gradle.api.plugins.UnknownPluginException

trait PluginUtils {
  abstract Project getProject()

  //Apply configuration only after some combination of plugins have been applied
  void withPlugins(List<String> ids, Closure config) {
    if(ids.empty || ids == null) return

    def pluginId = ids[0]

    if(ids.size() == 1) {
      withPluginId(pluginId, config)
    }

    withPlugins(ids.drop(1)) { plugin ->
      withPluginId(pluginId, config)
    }
  }

  void withAnyPlugin(List<String> ids, Closure config) {
    ids.each {
      //TODO: ensure config is only applied once
      withPluginId(it, config)
    }
  }

  //Wrapper for plugins.withId that allows configuring optional plugins that might not be on the classpath
  void withPluginId(String id, Closure config) {
    try {
      project.plugins.withId(id, config)
    } catch (UnknownPluginException e) {
      if(!e.message.contains(id)) {
        throw e
      }
    }
  }

  void withTask(String taskName, Closure action) {
    project.tasks.matching { it.name == taskName }.all(action)
  }
}
