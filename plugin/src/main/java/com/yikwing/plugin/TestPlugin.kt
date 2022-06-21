package com.yikwing.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val jiaguExt = project.extensions.create("yikwing", JiaGuBean::class.java)

        project.afterEvaluate {
            println(jiaguExt.userName)
            println(jiaguExt.password)


            val appExtension = project.extensions.getByType(AppExtension::class.java)

            println("buildToolsVersion ${appExtension.buildToolsVersion}")

            appExtension.applicationVariants.all { variant ->
                variant.outputs.all { baseVariant ->

                    val outputFile = baseVariant.outputFile

                    val name = baseVariant.name

                    project.tasks.create(
                        "jiagu${name}", JiaguTask::class.java,
                        outputFile, jiaguExt
                    )

                }
            }

        }


    }

}