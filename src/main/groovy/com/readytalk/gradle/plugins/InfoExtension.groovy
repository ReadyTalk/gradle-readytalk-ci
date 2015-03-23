package com.readytalk.gradle.plugins

class InfoExtension {
  Map<String,Object> props = [:]

  String branch = ''
  String buildStatus = 'integration'
  String ciProvider = 'none'
  String buildNumber = 'local'
  boolean isCI = false

  def propertyMissing(String name, value) {
    props.put(name,value)
  }

  def propertyMissing(String name) {
    props.get(name)
  }
}
