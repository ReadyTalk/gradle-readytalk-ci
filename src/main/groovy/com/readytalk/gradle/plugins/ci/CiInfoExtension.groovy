package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.ListenableExtension

class CiInfoExtension extends ListenableExtension {
  //private Map<String,List<Closure>> listeners = [:]
  //Map<String, Object> props = [:]

  String branch = ''
  String buildStatus = 'integration'
  String ciProvider = 'none'
  String buildNumber = 'local'
  boolean releaseBranch = false
  boolean masterBranch = false
  boolean release = false
  boolean ci = false

  /*def propertyMissing(String name, value) {
    props.put(name, value)
  }

  def propertyMissing(String name) {
    props.get(name)
  }

  //Auto binding value dependencies
  //TODO: Validate this actually makes sense
  //buildEnv.masterBranch { isMaster(branch) }
  //This means that master branch should get set both immediately and whenever buildEnv.branch changes
  //Now.. how to make this work for externals like version?
  //I'd need to store the base version string first, e.g. on plugin application
  //Or at least, on application of snapshots plugin...
  //buildEnv.watchProperty('release') { project.version = plugin.originalVersion + (release ? '' : '-SNAPSHOT') }

  //TODO: This functionality should probably disable itself once things have been written into the broker!
  //      E.g. we need a way to "freeze" the extension object
  //      E.g. we could allow adding new properties though
  //      Could even allow altering properties, but it should spit out a warning if that property had listeners. Maybe even an error
  //TODO: May want "read" listeners too, e.g. generate the result at a point in time
  //      Though really either works. A read version doesn't require freezing, but on the other hand, we'd want freezing anyways
  //      A reader version would make it easier to make changes based on external modifications (e.g. )
  //        Like what though? The only external source I care about are values set by the user or static at build initialization
  //        And the user values are basically to allow overrides in buildEnv
  //        It would be handy to add to a generic version of the class though
  //      The writer version makes it easier to propogate those changes and to freeze it (e.g. project.version)
  //      The freeze basically needs to happen as soon as properties are read by a non-updateable / static source

  //TODO: This NEEDS to have unit tests around it
  def invokeMethod(String name, args) {
    if(args.size() == 1 && args.first() instanceof Closure) {
      Closure setter = args.first()
      //Do not set delegate first, we only want to hit delegate if not otherwise found
      setter.delegate = ['getProperty': { String key ->
        addInternalSetter(name, key, setter)
      }] as GroovyObject
    } else {
      super.invokeMethod(name, args)
    }
  }

  private void addInternalSetter(String prop, String dependency, Closure listener) {
    //Throw away value because all needed values are either bound to the closure or part of the extension object
    watchProperty(dependency) { value ->
      this.setProperty(prop, listener.call())
    }
  }

  void setProperty(String key, String value) {
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
  }*/
}
