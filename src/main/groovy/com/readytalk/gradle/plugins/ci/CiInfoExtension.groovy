package com.readytalk.gradle.plugins.ci

class CiInfoExtension {
  Map<String, Object> props = [:]

  String branch = ''
  String buildStatus = 'integration'
  String ciProvider = 'none'
  String buildNumber = 'local'
  boolean releaseBranch = false
  boolean masterBranch = false
  boolean release = false
  boolean ci = false

  def propertyMissing(String name, value) {
    props.put(name, value)
  }

  def propertyMissing(String name) {
    props.get(name)
  }
}
