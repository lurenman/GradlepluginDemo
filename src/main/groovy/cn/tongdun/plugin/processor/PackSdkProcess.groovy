package cn.tongdun.plugin.processor

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.util.ProjectUtils
import cn.tongdun.plugin.util.TDFileUtils
import com.android.utils.FileUtils

import java.util.zip.ZipOutputStream

/**
 * 打包相关处理新增demo和压缩处理
 */
class PackSdkProcess implements Processor<ProcessorConfig> {
    def TAG = 'PackSdkProcess >'
    private BasePackSdkProcess processor

    def PackSdkProcess(BasePackSdkProcess processor) {
        this.processor = processor
    }

    @Override
    boolean handleProcess(ProcessorConfig config, ProcessorChain chain) {
        if (processor.handleProcess(config, chain)) {
            config.with {
                demoBuildProcess.call(it)
                zipPackSdk.call(it)
            }
            return true
        }
    }
    /**
     * 编译demo
     */
    private def demoBuildProcess = { ProcessorConfig config ->
        def taskBuildType = "assembleRelease"
        def demo_libs_path
        def demoModule
        if (config.sdkType == Constant.SDK_SAAS) {
            demo_libs_path = FileUtils.join(Constant.MODULE_DEMO_REL, "libs")
            demoModule = Constant.MODULE_DEMO_REL
        } else {
            demo_libs_path = FileUtils.join(Constant.MODULE_POC_DEMO, "libs")
            demoModule = Constant.MODULE_POC_DEMO
        }
        new File(demo_libs_path).with {
            if (exists())
                deleteDir()
        }
        ProjectUtils.copy(processor.dest_plus_aar_path, demo_libs_path)
        //开始编译demo,只编译全架构就可以
        ProjectUtils.gradleExec("./gradlew -q :${demoModule}:${taskBuildType}")
        def apk_path = FileUtils.join(demoModule, "build", "outputs", "apk", "release", demoModule + "-release.apk")
        ProjectUtils.copy(apk_path, processor.root_implement_path, "${demoModule}-${config.sdkVersion}.apk")
        println("${TAG} Build ${demoModule} success")
    }
    /**
     * 压缩文件
     */
    private def zipPackSdk = { ProcessorConfig config ->
        //构建build.zip
        def root_dev_build_path = FileUtils.join(processor.root_dev_path, 'build')
        ProjectUtils.copy('./nativeNameMap.txt', root_dev_build_path)
        ProjectUtils.gradleExec("cd " + processor.root_dev_path + " && zip -q -r build.zip build/")
        println("${TAG} zip build success")
        //构建fraudmetrix-base-***.zip
        def base_sdk_zip_path = FileUtils.join(processor.root_implement_path, "base", "fraudmetrix-base-${config.sdkVersion}.zip")
        def fileList = []
        fileList.add(new File(processor.out_shell_fm_path))
        fileList.add(new File(processor.dest_shell_so_path))

        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                new File(base_sdk_zip_path)), TDFileUtils.BUFF_SIZE))
        TDFileUtils.zipPluginFiles(fileList, zipout)
        zipout.close()
        println("${TAG} zip sdk base success")
        //构建fraudmetrix-plus-***.zip
        def plus_sdk_zip_path = FileUtils.join(processor.root_implement_path, "plus", "fraudmetrix-plus-${config.sdkVersion}.zip")
        fileList.with {
            it.add(new File(processor.out_readphone_fm_path))
            it.add(new File(processor.out_applist_fm_path))
            it.add(new File(processor.out_wifiinfo_fm_path))
            it.add(new File(processor.out_sensor_fm_path))
            it.add(new File(processor.out_location_fm_path))
        }
        zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                new File(plus_sdk_zip_path)), TDFileUtils.BUFF_SIZE))
        TDFileUtils.zipPluginFiles(fileList, zipout)
        zipout.close()
        println("${TAG} zip sdk plus success")
    }
}