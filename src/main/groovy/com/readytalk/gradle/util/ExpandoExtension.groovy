package com.readytalk.gradle.util

//TODO: Can this be a trait?
class ExpandoExtension {
  Map<String, Object> props = [:]

  def propertyMissing(String name, value) {
    props.put(name, value)
  }

  def propertyMissing(String name) {
    props.get(name)
  }
}
