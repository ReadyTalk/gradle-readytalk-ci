package com.readytalk.gradle.util

/**
 * Allows simple hooks to watch for property changes
 */

class ListenableExtension extends ExpandoExtension {
  static interface PropertyInterceptor {
    def propertyMissing(String key)
  }

  protected Map<String,List<Closure>> listeners = [:]

  void setProperty(String key, value) {
    super.setProperty(key, value)
    if(listeners.containsKey(key)) {
      listeners.get(key).each {
        it.call(value)
      }
    }
  }

  //If property updated, then call closure
  def watchProperty(String key, Closure listener) {
    if(!listeners.containsKey(key)) {
      listeners.put(key, [])
    }
    listeners.get(key).add(listener)
    if(this.getProperty(key) != null) {
      listener.call(this.getProperty(key))
    }
  }

  def watchProperties(Set<String> properties, Closure listener) {
    properties.each { String property ->
      watchProperty(property) { value ->
        listener.call(*(properties.collect(this.&getProperty)))
      }
    }
  }
}
