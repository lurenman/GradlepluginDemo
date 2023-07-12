package cn.tongdun.plugin.processor

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.util.ProjectUtils
import cn.tongdun.plugin.util.TDFileUtils
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.FileUtils
import org.gradle.api.Project

import java.util.concurrent.Callable

/**
 * base打包相关处理
 * WaitableExecutor
 */
class BasePackSdkProcess implements Processor<ProcessorConfig> {
    def TAG = 'BasePackSdkProcess >'
    def root_path
    def root_dev_path
    def root_implement_path
    def dest_shell_so_path
    def dest_plus_aar_path

    def out_shell_fm_path

    def out_applist_fm_path
    def out_readphone_fm_path
    def out_wifiinfo_fm_path
    def out_sensor_fm_path
    def out_location_fm_path

    @Override
    boolean handleProcess(ProcessorConfig config, ProcessorChain chain) {
        config.with {
            initFilePathDir.call(it)
            copyNativeSoFile.call(it)
            sha256soRecord.call(it)
            proguardModuleJar.call(it)
            sdkAarBuildProcess.call(it)
            TDFileUtils.operateSrctmpFile(it, false)
        }
        return true
    }
    /**
     *  初始化out文件路径
     */
    private def initFilePathDir = { ProcessorConfig config ->
        root_path = FileUtils.join('output', config.sdkVersion) + "-${config.sdkType}"
        root_dev_path = FileUtils.join(root_path, 'dev')
        root_implement_path = FileUtils.join(root_path, 'implement')
        println("${TAG} root_path:${root_path} root_dev_path:${root_dev_path} root_implement_path:${root_implement_path}")
        dest_shell_so_path = FileUtils.join(root_dev_path, "build", "so", "shell")
        new File(root_path).with {
            if (exists()) {
                deleteDir()
            }
        }
    }
    /**
     * 拷贝生成的so文件到out目录下
     */
    private def copyNativeSoFile = { ProcessorConfig config ->
        if (config.sdkType == Constant.SDK_PAAS) {
            // 企业级将接入文档打包至output
            def srcDocPath = FileUtils.join("doc", "paas", "pdf")
            def destDocPath = FileUtils.join(root_implement_path, 'doc')
            ProjectUtils.copy(srcDocPath, destDocPath)
        }
        //将sdk-shell jni so文件复制到output/**/dev/build/bak文件下的obj和libs中
        def dest_bak = FileUtils.join(root_dev_path, "build", "bak")
        def sdkShellProject = ProjectUtils.getProjectByName(Constant.MODULE_SDK_SHELL) as Project
        def sdkShellProjectPath = sdkShellProject.projectDir.getPath()

        FileUtils.join(sdkShellProjectPath, "src", "main", "obj").tap {
            ProjectUtils.copy(it, FileUtils.join(dest_bak, "obj"))
            it = FileUtils.join(sdkShellProjectPath, "src", "main", "libs")
            ProjectUtils.copy(it, FileUtils.join(dest_bak, "libs"))
            ProjectUtils.copy(it, dest_shell_so_path)
        }

        println("${TAG} Module native code so move into output")

    }
    /**
     * 记录sha256so的值
     */
    private def sha256soRecord = { ProcessorConfig config ->
        def soname = "libtongdun.so"
        def lines = [].tap {
            add('armeabi')
            add(ProjectUtils.gradleExec(ProjectUtils.rootProject, "${config.sha256Cmd} ${FileUtils.join(dest_shell_so_path, "armeabi", soname)}"))
            add('armeabi-v7a')
            add(ProjectUtils.gradleExec(ProjectUtils.rootProject, "${config.sha256Cmd} ${FileUtils.join(dest_shell_so_path, "armeabi-v7a", soname)}"))
            add('x86')
            add(ProjectUtils.gradleExec(ProjectUtils.rootProject, "${config.sha256Cmd} ${FileUtils.join(dest_shell_so_path, "x86", soname)}"))
            add('arm64-v8a')
            add(ProjectUtils.gradleExec(ProjectUtils.rootProject, "${config.sha256Cmd} ${FileUtils.join(dest_shell_so_path, "arm64-v8a", soname)}"))
        }

        def dest_bak = FileUtils.join(root_dev_path, "build", "bak")
        def mep_file = new File(FileUtils.join(dest_bak, "sec-map.txt"))
        mep_file.withWriter { writer ->
            lines.each { line ->
                writer.append(line + "\r\n")
            }
        }
        println("${TAG} Module native lib sha256 done")
    }
    /**
     * 混淆各模块jar
     */
    private def proguardModuleJar = { ProcessorConfig config ->
        def closureLists = [].tap {
            add({
                out_shell_fm_path = packProguardSdk(config, Constant.MODULE_SDK_SHELL, "shell")
            })
            add({
                out_readphone_fm_path = packProguardSdk(config, Constant.MODULE_SDK_READPHONE, "readphone")
            })
            add({
                out_applist_fm_path = packProguardSdk(config, Constant.MODULE_SDK_PACKAGELIST, "packagelist")
            })
            add({
                out_wifiinfo_fm_path = packProguardSdk(config, Constant.MODULE_SDK_WIFIINFO, "wifiinfo")
            })
            add({
                out_sensor_fm_path = packProguardSdk(config, Constant.MODULE_SDK_SENSOR, "sensor")
            })
            add({
                out_location_fm_path = packProguardSdk(config, Constant.MODULE_SDK_LOCATION, "location")
            })
        }
        def execuTask = {
            WaitableExecutor executor, List<Closure> list ->
                list.each { Closure closure ->
                    executor.execute(new Callable<String>() {
                        @Override
                        String call() throws Exception {
                            closure.call()
                        }
                    })
                }
        }
        WaitableExecutor.useGlobalSharedThreadPool().with {
            execuTask(it, closureLists)
            waitForTasksWithQuickFail(true)
        }
    }


    def packProguardSdk = {
        ProcessorConfig config, modulesdk, devsdkName ->
            println("${TAG} Module ${modulesdk} proguard start")
            def build_opt = config.buildType
            def dest_devsdk_path = FileUtils.join(root_dev_path, "build", "jar", devsdkName)
            def tfile = FileUtils.join(modulesdk, "build", "intermediates", "aar_main_jar", build_opt, "classes.jar")
            def jarName = "${devsdkName}.jar"
            if (modulesdk == Constant.MODULE_SDK_SHELL) {
                jarName = "${devsdkName}-${config.sdkVersion}.jar"
            }
            def sfile = FileUtils.join(dest_devsdk_path, "sdk-${jarName}")
            ProjectUtils.copy(tfile, dest_devsdk_path, "sdk-${jarName}")
            def out_fmsdk_path = FileUtils.join(dest_devsdk_path, "fm-${jarName}")
            if (config.buildType == Constant.BUILD_RELEASE) {
                def map_file_path = FileUtils.join(dest_devsdk_path, "mapping-" + devsdkName + ".txt")
                def command = "proguard @proguard-shell.pro -injars ${sfile} -outjars ${out_fmsdk_path} -libraryjars ${config.compileSdkVersionJarPath} -printmapping ${map_file_path}"
                ProjectUtils.gradleExec(ProjectUtils.rootProject, command)
            } else {
                def command = 'cp ' + sfile + ' ' + out_fmsdk_path
                ProjectUtils.gradleExec(ProjectUtils.rootProject, command)
            }
            println("${TAG} Module ${modulesdk} proguard end")
            out_fmsdk_path
    }
    /**
     * 通过sdk-aar module编译出aar
     */
    private def sdkAarBuildProcess = { ProcessorConfig config ->
        //删除目录libs/jniLibs目录
        def sdkaar_libs_path = FileUtils.join(Constant.MODULE_SDK_AAR, "libs")
        new File(sdkaar_libs_path).with {
            if (exists())
                deleteDir()
        }
        def sdkaar_jniLibs_path = FileUtils.join(Constant.MODULE_SDK_AAR, "src", "main", "jniLibs")
        new File(sdkaar_jniLibs_path).with {
            if (exists())
                deleteDir()
        }
        ProjectUtils.copy(out_shell_fm_path, sdkaar_libs_path)
        ProjectUtils.copy(dest_shell_so_path, sdkaar_jniLibs_path)

        //打base aar
        def taskBuildType = "assembleRelease"
        ProjectUtils.gradleExec("./gradlew -q :${Constant.MODULE_SDK_AAR}:${taskBuildType}")
        def outputs_aar_path = FileUtils.join(Constant.MODULE_SDK_AAR, "build", "outputs", "aar", "sdk-aar-" + Constant.BUILD_RELEASE + ".aar")

        def dest_base_path = FileUtils.join(root_implement_path, "base")
        def aarName = "fraudmetrix-base-${config.sdkVersion}.aar"
        ProjectUtils.copy(outputs_aar_path, dest_base_path, aarName)
        println("${TAG} Build sdk-aar base success")
        //打plus包
        ProjectUtils.copy(out_applist_fm_path, sdkaar_libs_path)
        ProjectUtils.copy(out_readphone_fm_path, sdkaar_libs_path)
        ProjectUtils.copy(out_wifiinfo_fm_path, sdkaar_libs_path)
        ProjectUtils.copy(out_sensor_fm_path, sdkaar_libs_path)
        ProjectUtils.copy(out_location_fm_path, sdkaar_libs_path)

        ProjectUtils.gradleExec("./gradlew -q :${Constant.MODULE_SDK_AAR}:${taskBuildType}")
        def dest_plus_path = FileUtils.join(root_implement_path, "plus")
        aarName = "fraudmetrix-plus-${config.sdkVersion}.aar"
        ProjectUtils.copy(outputs_aar_path, dest_plus_path, aarName)
        dest_plus_aar_path = FileUtils.join(dest_plus_path, aarName)
        println("${TAG} Build sdk-aar plus success")
    }
}