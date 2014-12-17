package com.readytalk.gradle.util

class StringUtils {
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
