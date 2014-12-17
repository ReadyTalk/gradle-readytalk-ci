package com.readytalk.gradle.util

class StringUtils {
  /**
   * Converts from snake case to camel case or manifest style
   *
   * @param snake Input string in snake_case
   * @param capitalized Whether to capitalize the first letter
   * @param delimiter Can be set to a '-' for manifest-style formatting
   * @return
   */
  static String snakeConvert(String snake, boolean capitalized = false, String delimiter = '') {
    def prop = snake.toLowerCase().replaceAll('(_)([A-z0-9])') {
      "${delimiter}${it[2].toUpperCase()}"
    }
    return capitalized ? prop.capitalize() : prop
  }

  /**
   * Converts from camel case to snake case or manifest style
   *
   * @param camel Input string in camelCase
   * @param capitalized Whether to capitalize the first letter of each sub word or lowercase everything
   * @param delimiter Can be set to a '-' for manifest-style formatting
   * @return
   */
  static String camelConvert(String camel, boolean capitalized = false, String delimiter = '_') {
    def prop = camel.replaceAll('([a-z])([A-Z])') {
      "${it[1]}${delimiter}${capitalized ? it[2].toUpperCase() : it[2].toLowerCase()}"
    }
    return capitalized ? prop.capitalize() : prop
  }
}
