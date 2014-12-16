package com.readytalk.gradle.util

import org.gradle.api.Project
import org.gradle.api.plugins.UnknownPluginException

//TODO: Convert this to a trait or abstract base plugin
class PluginUtils {
  //Apply configuration only after some combination of plugins have been applied
  static void withPlugins(final Project project, List<String> ids, Closure config) {
    if(ids.empty || ids == null) return

    def pluginId = ids[0]

    if(ids.size() == 1) {
      PluginUtils.withExtId(project, pluginId, config)
    }

    PluginUtils.withPlugins(project, ids.drop(1)) { plugin ->
      PluginUtils.withExtId(project, pluginId, config)
    }
  }

  static void withAnyPlugin(final Project project, List<String> ids, Closure config) {
    ids.each {
      //TODO: ensure config is only applied once
      PluginUtils.withExtId(project, it, config)
    }
  }

  //Wrapper for plugins.withId that allows configuring optional plugins that might not be on the classpath
  static void withExtId(final Project project, String id, Closure config) {
    try {
      project.plugins.withId(id, config)
    } catch (UnknownPluginException e) {
      if(!e.message.contains(id)) {
        throw e
      }
    }
  }

  static String snakeConvert(String snake, boolean capitalized = false, String delimiter = '') {
    def prop = snake.toLowerCase().replaceAll('(_)([A-z0-9])') {
      "${delimiter}${it[2].toUpperCase()}"
    }
    return capitalized ? prop.capitalize() : prop
  }

  static String camelConvert(String camel, boolean capitalized = false, String delimiter = '_') {
    def prop = camel.replaceAll('([a-z])([A-Z])') {
      "${it[1]}${delimiter}${capitalized ? it[2].toUpperCase() : it[2].toLowerCase()}"
    }
    return capitalized ? prop.capitalize() : prop
  }
}
