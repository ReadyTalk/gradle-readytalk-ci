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

  @Ignore
  def "allows chained closures for autobinding"() {

  }
}
