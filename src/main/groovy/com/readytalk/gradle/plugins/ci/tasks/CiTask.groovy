package com.readytalk.gradle.plugins.ci.tasks

import org.gradle.api.DefaultTask

class CiTask extends DefaultTask {
  public String group = 'ci'
  public String description = 'Main lifecycle task run at the check-in stage by the CI server.'
}
