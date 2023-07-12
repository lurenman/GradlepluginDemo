package cn.tongdun.plugin

import cn.tongdun.plugin.factory.TaskFactory
import cn.tongdun.plugin.util.ProjectUtils
import cn.tongdun.plugin.utils.ScanSetting
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginLaunch implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("-----------------------------------------")
        println("|                                       |")
        println("|            Tongdun Plugin!            |")
        println("|                                       |")
        println("-----------------------------------------")
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin)
        // 创建配置项
        project.extensions.create(ScanSetting.EXTENSIONS_NAME, PluginConfig)
        def name = project.getName()
        if (Constant.MODULE_SDK_SHELL.equals(name)) {
            ProjectUtils.rootProject = project.rootProject
            ProjectUtils.projectMap.clear()
            TaskFactory.registerTasks(project.rootProject.tasks)
        }
        if (isLibrary) {
            // 获取扩展中的配置
            def android = project.extensions.getByType(LibraryExtension)
            android.registerTransform(new TongdunTransform(project))
        }
    }

}
