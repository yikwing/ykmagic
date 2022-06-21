package com.yikwing.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * @Author yikwing
 * @Date 21/6/2022-09:15
 * @Description:
 */
open class JiaguTask @Inject constructor(file: File, jiaGuBean: JiaGuBean) : DefaultTask() {

    private val file: File
    private val jiaGuBean: JiaGuBean

    init {

        group = "jiagu"
        this.file = file
        this.jiaGuBean = jiaGuBean

    }


    @TaskAction
    fun adb() {
        project.exec { execSpec ->
            execSpec.commandLine("java", "-version")
        }

        project.exec { execSpec ->
            execSpec.commandLine("echo", file.absolutePath)
        }

        project.exec { execSpec ->
            execSpec.commandLine("echo", jiaGuBean.userName)
        }
    }


}