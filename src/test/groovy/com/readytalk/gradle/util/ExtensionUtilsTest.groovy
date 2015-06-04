package com.readytalk.gradle.util

import spock.lang.Ignore
import spock.lang.Specification

class ExtensionUtilsTest extends Specification {
  def "listenable propogates values"() {
    when:
    def obj = new ListenableExtension()
    obj.one = 'one'
    String first = obj.one
    String downstream = 'wrong'
    obj.watchProperty('one') { value ->
      downstream = value
    }
    boolean last = false
    obj.watchProperty('one') { value -> last = true }
    String second = downstream
    obj.one = 'two'
    String third = downstream

    then:
    first == 'one'
    second == 'one'
    third == 'two'
    assert last
  }

  def "listenable propogates values using autobind"() {
    when:
    def obj = new ListenableExtension()
    obj.alpha = 'one'
    obj.beta = 'two'
    obj.delta {
      alpha + beta + '!'
    }
    String first = obj.delta
    obj.alpha = 'Hello '
    String second = obj.delta
    obj.beta = 'World'
    String third = obj.delta

    then:
    obj.delta instanceof String
    first == 'onetwo!'
    second == 'Hello two!'
    third == 'Hello World!'
    obj.delta == 'Hello World!'
  }

  def "allows overwriting closure for same dependency"() {
    when:
    def obj = new ListenableExtension()
    obj.alpha = 'alpha'
    obj.delta { alpha }
    String first = obj.delta
    obj.delta { alpha + "beta" }
    String second = obj.delta
    obj.alpha = 'ALPHA'
    String third = obj.delta

    then:
    first == 'alpha'
    second == 'alphabeta'
    third == 'ALPHAbeta'
  }

  def "ignores stale dependencies"() {
    when:
    def obj = new ListenableExtension()
    obj.alpha = 'alpha'
    obj.beta = 'beta'
    obj.delta { alpha }
    String first = obj.delta
    obj.delta { beta }
    String second = obj.delta
    obj.beta = 'newvalue'
    String third = obj.delta
    //Should do nothing as delta should only be wired to beta now
    obj.alpha = 'ALPHA'
    String fourth = obj.delta

    then:
    first == 'alpha'
    second == 'beta'
    third == 'newvalue'
    fourth == 'newvalue'
  }

  def "can safely add listener to missing properties"() {
    when:
    def obj = new ListenableExtension()
    obj.watchProperty('alpha') { String alpha ->
      println "Hello world!"
    }

    then:
    noExceptionThrown()
  }

  def "optional properties can be set post facto"() {
    when:
    def obj = new ListenableExtension()
    String value = 'original'
    obj.watchProperty('alpha') { String alpha ->
      value = alpha
    }
    String first = value
    obj.alpha = 'newValue'
    String second = value

    then:
    first == 'original'
    second == 'newValue'
  }
}
