#*Changelog*
  * *0.6.0* [planned]
    - Use gradle-extended-info plugin for CI metadata

  * *0.5.0* [planned]
    - Properties in buildEnv now auto-recalculate on changes to dependent values (e.g. overriding branch will cause things like the release boolean to re-evaluate)
    - Version snapshot status now updates automatically instead of relying on afterEvaluate, so the correct version string should be available as early as possible
    - More robust Gradle 2.4 support for artifactory/bintray plugins
    - Allow prefix for release branch (e.g. origin/release_x.y.z)
    - [planned] explicit drone.io support

  * 0.4.3
    - Jenkins-related bugfixes

  * 0.4.2
    - Optional snapshot / buildnumber version strategy plugins
    - Refactored to improve sub-plugin names and separation of concerns
    - Fix nebula.info related exception on duplicate manifest entries

  * 0.3.0
    - Initial Gradle 2.4 support
