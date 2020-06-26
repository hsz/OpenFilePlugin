package com.github.hsz.openfileplugin.services

import com.intellij.openapi.project.Project
import com.github.hsz.openfileplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
