package com.readytalk.gradle.plugins.ci.version

import org.gradle.api.Project

class BuildNumberVersionPlugin extends AbstractVersionPlugin {
  void apply(final Project project) {
    super.apply(project)
    infoExt.watchProperties(['ci', 'buildNumber'] as Set<String>, { ci, buildNumber ->
      project.version = this.baseVersion + (ci ? "-${buildNumber}" : '-DEV')
      project.logger.info "CI status (${ci}) or buildNumber (${buildNumber}) changed, updating version string to ${project.version}"
    })
  }
}
