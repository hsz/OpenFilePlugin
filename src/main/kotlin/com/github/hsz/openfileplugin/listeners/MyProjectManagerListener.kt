package com.github.hsz.openfileplugin.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.hsz.openfileplugin.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.getService(MyProjectService::class.java)
    }
}
