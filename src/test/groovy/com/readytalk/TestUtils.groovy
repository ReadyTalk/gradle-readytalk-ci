package com.readytalk

import groovy.transform.CompileStatic
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.api.Task

trait TestUtils {
  abstract Project getProject()

  boolean hasDirectTaskDependency(String childTask, String parentTask) {
    return project.tasks[childTask].getTaskDependencies().getDependencies().find { Task t ->
      t.name.equals(parentTask)
    }
  }

  boolean hasTaskDependency(String childTask, String parentTask) {
    return project.tasks[childTask].getTaskDependencies().getDependencies().find { Task t ->
      t.name.equals(parentTask) ?: hasTaskDependency(t.name, parentTask)
    }
  }

  @CompileStatic
  //Create git repo in the test project directory that commits that contents + empty file
  Repository createMockGitRepo() {
    String projectPath = project.rootDir.absolutePath
    Repository repo = FileRepositoryBuilder.create(project.file("${projectPath}/.git"))

    repo.create()
    project.file("${projectPath}/code").createNewFile()
    new Git(repo).commit().setAll(true).setMessage("Initial Commit").call()

    return repo
  }
}
