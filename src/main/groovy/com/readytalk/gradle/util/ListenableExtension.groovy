package com.readytalk.gradle.util

/**
 *
 * This is similar to how InfoBrokerPlugin works in the nebula.info plugin
 * It's more complex though since it can affect project setup and not just metadata
 */
//TODO: Could this be a trait?
//TODO: Consider merging this logic with the similar construct in
class ListenableExtension extends ExpandoExtension {
  static interface PropertyInterceptor {
    def propertyMissing(String key)
  }

  private Map<String,List<Closure>> listeners = [:]
  private Map<String,Set<Closure>> internalEdge = [:]

  //TODO: Caveat - can't easily override this once wired up
  //      We could ensure the closures can chain, though this doesn't fix the referential opaqueness issue
  //      The problem is that we dynamically add the upstream dependency callbacks - they'd need to be fully encapsulated
  def invokeMethod(String name, args) {
    if(args.size() == 1 && args.first() instanceof Closure) {
      Closure setter = args.first()
      //Do not set delegate first, we only want to hit delegate if not otherwise found
      setter.delegate = ['propertyMissing': { String key ->
        addInternalSetter(name, key, setter)
      }] as PropertyInterceptor
      this.setProperty(name, setter.call())
    } else {
      super.invokeMethod(name, args)
    }
  }

  private def addInternalSetter(String prop, String dependency, Closure listener) {
    if(!internalEdge.containsKey(dependency)) internalEdge.put(dependency, [] as Set<Closure>)
    if(internalEdge.get(dependency).add(listener)) {
      watchProperty(dependency) { value ->
        //Throw away value param because all needed variables are either bound to the closure or part of the extension object
        this.setProperty(prop, listener.call())
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
      listeners.put(key, [listener])
    } else {
      listeners.get(key).add(listener)
    }
    listener.call(this.getProperty(key))
  }
}
