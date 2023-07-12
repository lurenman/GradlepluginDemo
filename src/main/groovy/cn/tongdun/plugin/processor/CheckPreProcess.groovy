package cn.tongdun.plugin.processor


import cn.tongdun.plugin.util.ProjectUtils
import com.android.utils.FileUtils
import org.gradle.api.Project

/**
 * 编译前的准备核实
 */
class CheckPreProcess implements Processor<ProcessorConfig> {
    def TAG = "CheckPreProcess >"

    @Override
    boolean handleProcess(ProcessorConfig config, ProcessorChain chain) {
        //1.获取系统平台
        def rootProject = ProjectUtils.getRootProject.call() as Project
        def host_os = ProjectUtils.gradleExec(rootProject, null, 'uname')
        config.host_os = host_os
        ProjectUtils.platform = host_os
        println(TAG + "the host_os : ${host_os}")
        if (host_os == "Linux") {
            config.ollvmPath = "linux-x86_64"
        }
        //2.判断环境
        def commandLists = ["proguard", "ndk-build", 'echo $PATH']
        def proguardPath = ProjectUtils.gradleExec(rootProject, "which ${commandLists[0]}")
        if (proguardPath == "") {
            println(TAG + ">> Install proguard first: brew install proguard")
            return false
        }
        println(TAG + "the environment proguardPath :${proguardPath}")
        def ndkbuildPath = ProjectUtils.gradleExec(rootProject, "which ${commandLists[1]}")
        if (ndkbuildPath == "") {
            println(">> Install and export android-ndk to PATH first")
            return false
        }
        println(TAG + "the environment ndkbuildPath :${ndkbuildPath}")

        //3.获取编译compile_sdk
        config.compileSdkVersion = ProjectUtils.getExtraProperties(rootProject).get("compileSdkVersion")
        println("CheckPreProcess the extensions find compileSdkVersion :" + config.compileSdkVersion)
        if (config.compileSdkVersion == null) {
            println(TAG + "the extensions not find compileSdkVersion")
            return false
        }
        //4.获取compileSdkVersionJarPath路径
        def echoPath = ProjectUtils.gradleExec(rootProject, commandLists[2])
        echoPath.split(':').each {
            def filename = FileUtils.join(it, 'platforms', 'android-' + config.compileSdkVersion, 'android.jar')
            if (new File(filename).exists()) {
                config.compileSdkVersionJarPath = filename
                return true
            }
        }
        if (config.compileSdkVersionJarPath == null) {
            println(TAG + ">> Install and export android-sdk to PATH first")
            return false
        }
        println(TAG + "the config.compileSdkVersionJarPath : ${config.compileSdkVersionJarPath}")
        return chain.process(config, chain)
    }
}