package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.Publication
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.util.GradleVersion

class CiPublishingPlugin implements Plugin<Project>, PluginUtils {
  private CiInfoExtension infoExt
  Project project

  void apply(final Project project) {
    this.project = project
    project.plugins.with {
      apply(BasePlugin)
      def infoPlugin = apply(CiInfoPlugin)
      this.infoExt = infoPlugin.extension
    }

    configurePublishing()
    configureLegacyArtifactoryPublish()
    configurePublishTask()

    //TODO: Optional default publications for java/web components
  }

  private configureLegacyArtifactoryPublish() {
    //Support for older publishing style
    project.with {
      withPluginId('artifactory') {
        project.tasks.maybeCreate('publish').configure {
          group = 'publishing'
        }
        project.tasks[CiLifecyclePlugin.CI_LIFECYCLE_TASK_NAME].dependsOn project.tasks.publish
        project.tasks.publish.dependsOn project.tasks.artifactoryPublish
      }
    }
  }

  private configurePublishTask() {
    project.with {
      withTask(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME) { Task publishTask ->
        withTask('build') { Task buildTask ->
          publishTask.dependsOn buildTask
        }
      }
    }
  }

  private void configurePublishing() {
    configurePluginPublishing()
    configureMavenPublishing()
    configureIvyPublishing()
    withAnyPlugin(['ivy-publish', 'maven-publish']) {
      configureBintrayPublishing()
      configureArtifactoryPublishing()
    }
    //TODO: Snapshot verification
  }

  private void configurePluginPublishing() {
    project.with {
      plugins.withId('com.gradle.plugin-publish') {
        // Add maven publish for local install and SNAPSHOT publishing
        plugins.apply(MavenPublishPlugin)
        tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME).dependsOn tasks.getByName('publishPlugins')
        tasks.'publishPlugins'.mustRunAfter 'build'
        // Only publish plugin releases.
        tasks.'publishPlugins'.onlyIf { infoExt.isRelease() }
      }
      /*TODO: Add maven publications:
        publications {
          maven(MavenPublication) {
            from components.java
            artifact publishPluginJar {
              classifier 'sources'
            }
            artifact publishPluginGroovyDocsJar {
              classifier 'groovydoc'
            }
            artifact publishPluginJavaDocsJar {
              classifier 'javadoc'
            }
          }
        }
      */
    }
  }

//Map local ivy publishing to install task
  private void configureIvyPublishing() {
    project.with {
      plugins.withId('ivy-publish') {
        //Use project local repo if present (lazy)
        project.repositories.matching { ArtifactRepository repo ->
          repo.name == 'local' && (repo instanceof IvyArtifactRepository)
        }.all { IvyArtifactRepository ivyLocalRepo ->
          publishing {
            repositories {
              add ivyLocalRepo
            }
          }
        }

        //TODO: Combine with repo logic to only wire up anything if a local ivy repo is actually present
        ensureInstallTask('ivy')

        publishing {
          publications.withType(IvyPublication) { IvyPublication pub ->
            String pubTaskName = "publish${pub.name.capitalize()}PublicationToLocalRepository"
            boolean doInstall = project.gradle.startParameter.taskNames.any {
              it == 'install' || it.endsWith(':install')
            }
            if (GradleVersion.current() > GradleVersion.version('2.3')) {
              model {
                tasks.install {
                  dependsOn pubTaskName
                }
                tasks."${pubTaskName}" {
                  onlyIf { doInstall }
                }
              }
            } else {
              tasks.withType(PublishToIvyRepository) { PublishToIvyRepository pubTask ->
                pubTask.onlyIf { doInstall }
                tasks.install.dependsOn pubTask
              }
            }
          }
        }
      }
    }
  }

  //Map local maven publishing to install task
  private void configureMavenPublishing() {
    project.with {
      plugins.withId('maven-publish') {
        ensureInstallTask('maven')
        withTask('install') {
          tasks.install.dependsOn(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME)
        }
      }
    }
  }

  private void configureArtifactoryPublishing() {
    project.with {
      withPluginId('com.jfrog.artifactory') {
        //Ensure publishing goes through artifactoryPublish when using artifactory plugin
        tasks[PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME].dependsOn tasks['artifactoryPublish']
        tasks['artifactoryPublish'].mustRunAfter 'build'

        //Oddly, artifactory plugin doesn't always wire these itself
        plugins.withId('ivy-publish') {
          tasks['artifactoryPublish'].dependsOn tasks.withType(GenerateIvyDescriptor)
        }
        plugins.withId('maven-publish') {
          tasks['artifactoryPublish'].dependsOn tasks.withType(GenerateMavenPom)
        }

        //Auto map publications to artifactory publishing
        publishing {
          publications.all { Publication pub ->
            modelPublicationWiring(pub, 'artifactoryPublish')
            project.tasks.artifactoryPublish { pubTask ->
              pubTask.publications(pub.name)
            }
          }
        }
      }
    }
  }

  private void configureBintrayPublishing() {
    project.with {
      withPluginId('com.jfrog.bintray') {
        if(GradleVersion.current() < GradleVersion.version('2.4')) {
          logger.warn('com.readytalk.ci.publishing: bintray auto-wiring support limited for Gradle < 2.4')
        }

        plugins.withId('maven-publish') {
          tasks.withType(GenerateMavenPom) { pomTask ->
            tasks.'bintrayUpload'.dependsOn pomTask
          }
        }

        tasks.getByName(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME).dependsOn tasks.getByName('bintrayUpload')
        tasks.'bintrayUpload'.mustRunAfter 'build'
        // Bintray only handles releases by default
        tasks.'bintrayUpload'.onlyIf { infoExt.isRelease() }
        publishing {
          publications.all { Publication pub ->
            //Gradle <= 2.3
            bintray.publications += pub.name
            //Gradle >= 2.4
            modelPublicationWiring(pub, 'bintrayUpload')
          }
        }
      }
    }
  }

  private Set<Task> tasksForPublication(Publication pub) {
    if(pub instanceof MavenPublication || pub instanceof IvyPublication) {
      return (pub.getArtifacts().collect { artifact ->
        artifact.getBuildDependencies().getDependencies()
      }.flatten())
    } else {
      logger.warn "Publication ${pub.getName()} is not an ivy or maven artifact, ignoring request for task dependencies."
      return [] as Set<Task>
    }
  }

  private void minGradleVersion(String versionString, Closure config) {
    if(GradleVersion.current() >= GradleVersion.version(versionString)) {
      config.call()
    }
  }

  //Cause the named task to depend on all tasks Gradle reports as being required for the given publication
  //Used as a workaround for jfrog publishing not playing nice with Gradle 2.4+
  private void modelPublicationWiring(Publication publication, String publishTaskName) {
    minGradleVersion('2.4') {
      project.model {
        tasks."${publishTaskName}" {
          dependsOn tasksForPublication(publication)
        }
      }
    }
  }

  //Create and wire up install task
  private void ensureInstallTask(String style = '') {
    //TODO: Verify install publish tasks can't accidentally run before checks finish
    project.tasks.replace('install').configure {
      description = "Install project into the local ${style} repository"
      group = "publishing"
    }
    withTask('install') {
      project.tasks.install.dependsOn project.tasks.'build'
    }
  }
}
