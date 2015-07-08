#*Changelog*
  * *0.6.0 - 1.0.0* [planned]
    - Use gradle-extended-info plugin for CI metadata
    - explicit drone.io support

  * 0.5.2
    - Fix for artifactoryPublish in Gradle 2.5

  * 0.5.1
    - Smarter config for legacy artifactory-upload plugin

  * 0.5.0
    - Properties in buildEnv now auto-recalculate on changes to dependent values (e.g. overriding branch will cause things like the release boolean to re-evaluate)
    - Version snapshot status now updates automatically instead of relying on afterEvaluate, so the correct version string should be available as early as possible
      NOTE: version expected to be set before plugins applied - if not, you'll need to set buildEnv.baseVersion
    - Allow prefix for release branch (e.g. origin/release_x.y.z)
    - Expanded artifactory metadata
    - Check both id strings for legacy artifactory upload plugin

  * 0.4.3
    - Jenkins-related bugfixes

  * 0.4.2
    - Optional snapshot / buildnumber version strategy plugins
    - Refactored to improve sub-plugin names and separation of concerns
    - Fix nebula.info related exception on duplicate manifest entries

  * 0.3.0
    - Initial Gradle 2.4 support
