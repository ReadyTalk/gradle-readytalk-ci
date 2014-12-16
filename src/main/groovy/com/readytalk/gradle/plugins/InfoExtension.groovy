package com.readytalk.gradle.plugins

//TODO: Allow predefined fields - last time I tried this it crashed the groovy runtime
class InfoExtension {
  Map<String,Object> properties = [:]

  def propertyMissing(String name, value) {
    properties[name] = value
  }

  def propertyMissing(String name) {
    properties[name]
  }
}