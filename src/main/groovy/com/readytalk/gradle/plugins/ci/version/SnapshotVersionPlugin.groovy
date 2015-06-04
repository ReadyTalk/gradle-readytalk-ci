package com.readytalk.gradle.plugins.ci.version

import org.gradle.api.Project

class SnapshotVersionPlugin extends AbstractVersionPlugin {
  void apply(final Project project) {
    super.apply(project)
    infoExt.watchProperty('release', { boolean release ->
      project.version = this.baseVersion + (release ? '' : '-SNAPSHOT')
      project.logger.info "release status set to ${release}, updating version to ${project.version}"
    })
  }
}
