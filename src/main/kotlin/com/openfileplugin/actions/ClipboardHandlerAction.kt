package com.openfileplugin.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ex.ClipboardUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.io.URLUtil
import com.openfileplugin.OpenFileBundle
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking

class ClipboardHandlerAction : AnAction() {

    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer()
            expectSuccess = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val text = ClipboardUtil.getTextInClipboard() ?: return project.notify(OpenFileBundle.message("emptyClipboard"))
        val lineRegex = """^ on line (\d+)""".toRegex()
        val host = "https://www.openfileplugin.com/open.php"

        URLUtil.URL_PATTERN.toRegex().findAll(text).forEach {
            val url = it.value
            val line = lineRegex.find(text.substring(it.range.last + 1))?.groupValues?.get(1)
            val path = runBlocking {
                client.post<String>(host) {
                    body = MultiPartFormDataContent(formData {
                        append("scan", listOfNotNull(url, line).joinToString(":"))
                    })
                }
            }

            val file = VirtualFileManager.getInstance().findFileByUrl(path)
                ?: return project.notify(OpenFileBundle.message("localFileNotFound", path))

            FileEditorManager.getInstance(project).openFile(file, true)
        }
    }

    private fun Project.notify(message: String) {
        val notification = Notification("OpenFile", "OpenFile", message, NotificationType.INFORMATION)
        Notifications.Bus.notify(notification, this)
    }
}
