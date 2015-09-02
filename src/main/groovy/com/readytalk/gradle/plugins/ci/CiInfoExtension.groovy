package com.readytalk.gradle.plugins.ci

import com.readytalk.gradle.util.ListenableExtension

class CiInfoExtension extends ListenableExtension {
  String branch = ''
  String buildStatus = 'integration'
  String ciProvider = 'none'
  String buildNumber = 'local'
  boolean releaseBranch = false
  boolean masterBranch = false
  boolean release = false
  boolean ci = false
}
