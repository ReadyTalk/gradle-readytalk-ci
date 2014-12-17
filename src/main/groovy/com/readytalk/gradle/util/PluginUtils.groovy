package com.readytalk.gradle.util

import org.gradle.api.Project
import org.gradle.api.plugins.UnknownPluginException

trait PluginUtils {
  abstract Project getProject()

  /**
   * Apply configuration only after some combination of plugins have been applied
   *
   * @param ids List of plugins that must be applied before executing config closure
   * @param config Configuration closure
   */
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

  /**
   * Apply configuration if any of a set of plugins is applied
   *
   * @param ids List of plugin ids
   * @param config Configuration to apply
   */
  void withAnyPlugin(List<String> ids, Closure config) {
    ids.each {
      //TODO: ensure config is only applied once
      withPluginId(it, config)
    }
  }

  /**
   * Wrapper for plugins.withId that allows configuring optional plugins that might not be on the classpath
   *
   * @param id Plugin id string
   * @param config Configuration to apply
   */
  void withPluginId(String id, Closure config) {
    try {
      project.plugins.withId(id, config)
    } catch (UnknownPluginException e) {
      if(!e.message.contains(id)) {
        throw e
      }
    }
  }

  /**
   * Like tasks.withType, except with task name instead of task type
   * Convenience method for lazy configuration related to a task
   *
   * @param taskName Name of task to run against if present/added
   * @param action Configuration closure
   */
  void withTask(String taskName, Closure action) {
    project.tasks.matching { it.name == taskName }.all(action)
  }
}
