package com.readytalk.gradle.plugins

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.publish.Publication
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.util.GradleVersion

class CiPublishingPlugin implements Plugin<Project>, PluginUtils {
  Project project

  void apply(final Project project) {
    this.project = project

    withAnyPlugin(['ivy-publish', 'maven-publish']) {
      configurePublishing()
    }

    //Support for older publishing style
    withPluginId('artifactory') {
      project.tasks.maybeCreate('publish').configure {
        group = 'publishing'
      }
      project.tasks[CiLifecyclePlugin.CI_TASK].dependsOn project.tasks.publish
      project.tasks.publish.dependsOn project.tasks.artifactoryPublish
    }

    withTask(CiLifecyclePlugin.PUBLISH_TASK) { Task publishTask ->
      withTask('build') { Task buildTask ->
        publishTask.dependsOn buildTask
      }
    }

    //TODO: Optional default publications for java/web components
  }

  void configurePublishing() {
    project.with {
      withPluginId('com.jfrog.bintray') {
        //TODO: Expand bintray support
        logger.info('Bintray support limited to publish wiring only')
        tasks.getByName(CiLifecyclePlugin.PUBLISH_TASK).dependsOn tasks.getByName('bintrayUpload')
        tasks['bintrayUpload'].mustRunAfter 'build'
      }

      withPluginId('com.jfrog.artifactory') {
        //Ensure publishing goes through artifactoryPublish when using artifactory plugin
        tasks[CiLifecyclePlugin.PUBLISH_TASK].dependsOn tasks['artifactoryPublish']
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
            project.tasks.artifactoryPublish { pubTask ->
              pubTask.publications(pub.name)
            }
          }
        }
      }

      //Map local maven publishing to install task
      plugins.withId('maven-publish') {
        ensureInstallTask('maven')
        withTask('install') {
          tasks.install.dependsOn(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME)
        }
      }

      //Map local ivy publishing to install task
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
            boolean doInstall = project.gradle.startParameter.taskNames.any { it == 'install' || it.endsWith(':install') }
            if(GradleVersion.current() > GradleVersion.version('2.3')) {
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

    //TODO: Snapshot verification
  }

  //Create and wire up install task
  void ensureInstallTask(String style = '') {
    //TODO: Verify install publish tasks can't accidentally run before checks finish
    project.tasks.replace('install').configure {
      description = "Install project into the local ${style} repository"
      group = "publishing"
    }
    withTask('install') {
      project.tasks.install.dependsOn project.tasks.build
    }
  }
}
