package com.readytalk.gradle.plugins

import com.readytalk.gradle.util.PluginUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownRepositoryException
import org.gradle.api.publish.ivy.tasks.GenerateIvyDescriptor
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal

class CiPublishingPlugin implements Plugin<Project> {
  private Project project

  void apply(final Project project) {
    this.project = project

    PluginUtils.withAnyPlugin(project, ['ivy-publish', 'maven-publish']) {
      configurePublishing()
    }

    //Support for older publishing style
    PluginUtils.withExtId(project, 'artifactory') {
      project.tasks.maybeCreate('publish').configure {
        group = 'publishing'
      }
      project.tasks[CiLifecyclePlugin.CI_TASK_NAME].dependsOn project.tasks.publish
      project.tasks.publish.dependsOn project.tasks.artifactoryPublish
    }
  }

  void configurePublishing() {
    project.with {
      PluginUtils.withExtId(project, 'com.jfrog.bintray') {
        //TODO: Expand bintray support
        logger.info('Bintray support limited to publish wiring only')
        tasks.getByName(CiLifecyclePlugin.PUBLISH_TASK_NAME).dependsOn tasks.getByName('bintrayUpload')
        tasks['bintrayUpload'].mustRunAfter 'build'
      }

      PluginUtils.withExtId(project, 'com.jfrog.artifactory') {
        //Ensure publishing goes through artifactoryPublish when using artifactory plugin
        tasks[CiLifecyclePlugin.PUBLISH_TASK_NAME].dependsOn tasks['artifactoryPublish']
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
          publications.all { pub ->
            project.tasks.artifactoryPublish { pubTask ->
              pubTask.publications(pub.name)
            }
          }
        }
      }

      //Map local maven publishing to install task
      plugins.withId('maven-publish') {
        ensureInstallTask('maven')
        tasks.withType(PublishToMavenLocal) { pubTask ->
          tasks.install.dependsOn pubTask
        }
      }

      //Map local ivy publishing to install task
      plugins.withId('ivy-publish') {
        //Use project local repo if present
        publishing {
          try {
            repositories {
              //TODO: Verify this is actually an ivy repository
              add project.repositories.getByName('local')
            }
          } catch (UnknownRepositoryException e) {
            logger.info("No local ivy repo found; install task will do nothing")
          }
        }

        //TODO: Combine with repo logic to only wire up anything if a local ivy repo is actually present
        //      Also should validate that 'local' is actually a local ivy repo
        ensureInstallTask('ivy')

        tasks.withType(PublishToIvyRepository) { pubTask ->
          //Assume ivy local repo is called 'local'
          if(pubTask.name.endsWith('PublicationToLocalRepository')) {
            pubTask.onlyIf {
              project.gradle.startParameter.taskNames.contains('install')
            }
            tasks.install.dependsOn pubTask
          }
        }
      }
    }

    //TODO: Snapshot verification
  }

  //Create and wire up install task
  void ensureInstallTask(String style = '') {
    project.with {
      tasks.replace('install').configure {
        description = "Install project into the local ${style} repository"
        group = "publishing"
        dependsOn tasks.matching { it.name.equals('build') }
        //TODO: Verify this doesn't allow build to happen after it's already published
      }
    }
  }
}
