package com.readytalk

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
}
