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
