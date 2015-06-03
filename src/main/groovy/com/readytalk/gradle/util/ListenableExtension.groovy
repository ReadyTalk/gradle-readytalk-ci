package com.readytalk.gradle.util

import org.gradle.api.CircularReferenceException

/**
 * Expando extension object that allows forwarding property changes
 *
 * This is similar to how InfoBrokerPlugin works in the nebula.info plugin
 * It's more complex though since it can affect project setup and not just metadata
 */
//TODO: Could this be a trait?
class ListenableExtension extends ExpandoExtension {
  static interface PropertyInterceptor {
    def propertyMissing(String key)
  }

  protected Map<String,List<Closure>> listeners = [:]
  protected Map<String,Set<Closure>> internalEdge = [:]
  protected Map<String,Set<String>> dependencyMap = [:]

  //TODO: Caveat - can't easily override this once wired up
  def addSetter(String property, Closure setter) {
    Set<String> dependencies = [] as Set<String>

    setter.delegate = ['propertyMissing': { Set<String> deps, String key ->
      deps.add(key)
      addInternalSetter(property, key, setter)
    }.curry(dependencies)] as PropertyInterceptor

    //Store dependency map to detect degenerate edge case
    dependencyMap.put(property, dependencies)

    this.setProperty(property, setter.call())
  }

  //Syntactic sugar
  def methodMissing(String name, args) {
    if(args.size() == 1 && args.first() instanceof Closure) {
      addSetter(name, args.first())
    } else {
      throw new MissingMethodException(name, this.class, args)
    }
  }

  protected def addInternalSetter(String prop, String dependency, Closure listener) {
    if(prop == dependency) {
      throw new CircularReferenceException("Property cannot depend on itself (${prop})")
    }

    if(!internalEdge.containsKey(dependency)) {
      internalEdge.put(dependency, [] as Set<Closure>)
    }

    if(internalEdge.get(dependency).add(listener)) {
      watchProperty(dependency) { value ->
        //Throw away value param because all needed variables are either bound to the closure or part of the extension object
        if(dependencyMap.get(prop)?.contains(dependency)) {
          this.setProperty(prop, listener.call())
        } else {
          //TODO: Currently, can only properly overwrite autobinding closure if new closure references same dependent properties
          println "FIXME: ignoring stale property listener for ${dependency} -> ${prop}"
        }
      }
    }

    //in either case, we still need to return the dependency's actual value
    return this.getProperty(dependency)
  }

  void setProperty(String key, value) {
    super.setProperty(key, value)
    if(listeners.containsKey(key)) {
      listeners.get(key).each {
        it.call(value)
      }
    }
  }

  //TODO: Should we have an autobinding version of this too?
  def watchProperty(String key, Closure listener) {
    if(!listeners.containsKey(key)) {
      listeners.put(key, [])
    }
    listeners.get(key).add(listener)
    listener.call(this.getProperty(key))
  }

  def watchProperties(Set<String> properties, Closure listener) {
    properties.each { String property ->
      watchProperty(property) { value ->
        listener.call(*(properties.collect(this.&getProperty)))
      }
    }
  }
}