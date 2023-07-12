package cn.tongdun.plugin.util

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.file.CopySpec
import org.gradle.process.ExecSpec

/**
 * project工具类，后续考虑扩展
 */
class ProjectUtils {
    //保存注册的project集合
    public static def Project rootProject
    //享元共享project
    public static def projectMap = [:]

    public static def platform = "Darwin"

    /**
     * 获取rootProject
     */
    static def getRootProject = {
        rootProject
    }
    /**
     * 获取project扩展属性
     */
    static def getExtraProperties = {
        Project rootProject ->
            rootProject.getExtensions().getExtraProperties()
    }

    /**
     * 执行gradle外部命令
     * @param self
     * @param host
     * @param command
     * @return
     */
    static def gradleExec(Project self = rootProject, def host = platform, def command) {
        def out = new ByteArrayOutputStream()
        self.exec { ExecSpec execSpec ->
            execSpec.with {
                setExecutable('bash')
                if (host == null) {
                    setArgs(['-c', command])
                } else {
                    setArgs(['-c', "source ~/.bash_profile" + "&&" + command])
                }
                setStandardOutput(out)
            }
        }
        out.toString()
    }
    /**
     * 获取属性的值
     * @param self
     * @param key
     * @param closure
     * @return
     */
    static def getPropertiesValue(Project self, def key, Closure closure) {
        self.properties.each { def skey, def svalue ->
            if (skey == key) {
                closure(svalue)
                return true
            }
        }
    }
    /**
     * 打印project相关属性值
     */
    static def printPropertiesValue = {
        Project self ->
            self.properties.each { skey, def svalue ->
                println("${self.name} project properties key:${skey} value:${svalue}")
            }
    }
    /**
     * 获取Android.mk 文件路径
     */
    static def getAndroidmkPath = {
        Project project ->
            def android = project.getExtensions().findByName("android") as LibraryExtension
            def externalNativeBuild = android.getProperties().find { key, value ->
                key == "externalNativeBuild"
            }
            externalNativeBuild.getValue().getAt("ndkBuild").getAt("path")
    }
    /**
     * 获取jni.srcDirs
     */
    static def getJniSrcDirs = {
        Project project ->
            def jni = project.getProperties().get('android').getAt('sourceSets').getAt('main').getAt('jni') as DefaultAndroidSourceDirectorySet
            jni.getSrcDirs()
    }
    /**
     * 设置jni.srcDirs
     */
    static def setJniSrcDirs = {
        Project project ->
            def jni = project.getProperties().get('android').getAt('sourceSets').getAt('main').getAt('jni') as DefaultAndroidSourceDirectorySet
            jni.setSrcDirs([])
    }

    /**
     * 通过project name 获取project
     */
    static def getProjectByName = {
        def name ->
            def project = projectMap.get(name)
            if (project != null)
                return project
            def rootProject = getRootProject.call() as Project
            def subprojects = rootProject.getSubprojects()
            project = subprojects.find {
                it.name == name
            }
            if (project != null)
                projectMap.put(name, project)
            return project
    }
    /**
     * 简单的copy文件操作
     */
    static def copy = { def srcPath, def destPath, def rename = null ->
        rootProject.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.from(srcPath)
                copySpec.into(destPath)
                if (rename != null) {
                    copySpec.rename(new Transformer<String, String>() {
                        @Override
                        String transform(String s) {
                            return rename
                        }
                    })
                }
            }
        })
    }
}