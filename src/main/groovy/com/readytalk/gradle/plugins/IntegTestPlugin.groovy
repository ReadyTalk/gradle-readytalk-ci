package com.readytalk.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class IntegTestPlugin implements Plugin<Project> {
  void apply(final Project project) {
    project.with {
      plugins.withId('java') {
        sourceSets {
          integTest {
            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
          }
        }

        configurations {
          integTestCompile.extendsFrom testCompile
          integTestRuntime.extendsFrom testRuntime
        }

        //TODO: Figure out why this syntax doens't work
        //task integTest(type: Test, group: 'Verification') {
        tasks.create('integTest', Test.class) {
          group = 'verification'
          reports {
            html.destination = "${testReportDir}/integTest"
            junitXml.destination = "${testResultsDir}/integTest"
          }
          classpath = sourceSets.integTest.runtimeClasspath
          testClassesDir = sourceSets.integTest.output.classesDir

          //Unit tests should run first
          shouldRunAfter tasks.test

          beforeTest { testName ->
            logger.info("Running integTest: ${testName}")
          }

          doFirst {
            if(files(sourceSets.integTest.getAllSource()).isEmpty()) {
              logger.info("${name} does not contain any integration tests")
            }
          }
        }

        plugins.withId('idea') {
          afterEvaluate {
            idea {
              module {
                testSourceDirs += sourceSets.integTest.java.srcDirs
                scopes.TEST.plus.add(configurations.integTestRuntime)
                scopes.TEST.plus.add(configurations.integTestCompile)
              }
            }
          }
        }

        plugins.withId('eclipse') {
          eclipse {
            classpath {
              plusConfigurations.add(configurations.integTestCompile)
              plusConfigurations.add(configurations.integTestRuntime)
            }
          }
        }
      }
    }
  }
}
