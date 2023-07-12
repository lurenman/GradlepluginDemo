package cn.tongdun.plugin.processor

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.generator.DictionaryGenerator
import cn.tongdun.plugin.generator.ObfuscateGenerator
import cn.tongdun.plugin.util.ProjectUtils
import cn.tongdun.plugin.util.TDFileUtils
import com.android.utils.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 编译处理
 */
class BuildModuleProcess implements Processor<ProcessorConfig> {
    def TAG = 'BuildModuleProcess >'
    def mLatch = new CountDownLatch(1)


    @Override
    boolean handleProcess(ProcessorConfig config, ProcessorChain chain) {
        build_prepare(config)
        dealenterprice(config)
        if (config.buildType == Constant.BUILD_RELEASE) {
            generation_dictt.call()
            runScript(config)
        }
        build_module(config)
        return chain.process(config, chain)
    }

    /**
     * 获取sdk版本号 例如sdkVersion = 410
     * 操作Android.mk的log开关
     * @param config
     * @return
     */
    def build_prepare = { ProcessorConfig config ->
        def commonProject = ProjectUtils.getProjectByName("common")
        def file = commonProject.file("src/main/java/cn/tongdun/android/common/setting/Constants.java")
        if (!file.exists())
            throw GradleException("error common project no Constants.java file")
        def version = TDFileUtils.readProp(file, "VERSION", "=") as String
        version.with {
            it = it.findAll('\\d..*\\d').pop()
            config.sdkVersion = it
            println TAG + "config.sdkVersion:" + it
        }
        TDFileUtils.operateSrctmpFile(config, true)
        //替换Android.mk 文件开关操作
        def sdkShellProject = ProjectUtils.getProjectByName(Constant.MODULE_SDK_SHELL) as Project
        def androidmkPath = ProjectUtils.getAndroidmkPath(sdkShellProject) as String
        def jniSrcDirs = ProjectUtils.getJniSrcDirs(sdkShellProject)
        config.jniSrcDirs = jniSrcDirs
        println TAG + " jniSrcDirs :" + jniSrcDirs
        new File(androidmkPath).with { def f ->
            println TAG + " Android.mk Path :" + f.getPath()
            if (config.logSwitch) {
                TDFileUtils.replaceProp(f, "#logs=open", "logs=open")
                println TAG + "Open ndk log"
            } else {
                TDFileUtils.replaceProp(f, "logs=open", "#logs=open")
                println TAG + "Close ndk log"
            }
        }
    }
    /**
     * 文件内容，目前只有企业级改，后续改成变体操作
     * @param config
     */
    def dealenterprice = {
        ProcessorConfig config ->
            if (config.sdkType == Constant.SDK_PAAS) {
                println TAG + "> start build enterprise"
                def sdkShellProject = ProjectUtils.getProjectByName(Constant.MODULE_SDK_SHELL) as Project
                def sdkShellProjectPath = sdkShellProject.projectDir.getPath()

                sdkShellProjectPath.with { def path ->
                    def tdcore_file = new File(FileUtils.join(path, "src/main/java/cn/tongdun/android/core/FMCore.java"))
                    tdcore_file.with {
                        TDFileUtils.replaceProp(it, "//params.put(Constants.CHANNEL, Config.APPNAME);", "params.put(Constants.CHANNEL, Config.APPNAME);")
                        //修改FMCORE里面引用FMAgent.initWithOptions
                        TDFileUtils.replaceProp(it, "FMAgent.initWithOptions(context,FMAgent.ENV_PRODUCTION,FMAgent.mOptions);", "FMAgent.init(context,Config.CUSTOM_URL);")
                    }

                    def tdshell_file = new File(FileUtils.join(path, "src/main/java/cn/tongdun/android/shell", "FMAgent.java"))
                    tdshell_file.with {
                        //删除FMAgent的init(context,env)方法
                        TDFileUtils.replaceProp(it, "public static void init(Context context, String env) {", "private static void init1(Context context, String env)  {")
                        //解注释FMAgent的init(Context,url)方法
                        TDFileUtils.replaceProp(it, "private static void init1(Context context, String url) {", "public static void init(Context context, String url)  {")
                        //将initWithCallback由public 变更为private
                        TDFileUtils.replaceProp(it, "public static void initWithCallback(Context context, String env, FMCallback fmCallback) {", "public static void initWithCallback(Context context, String url, FMCallback fmCallback) {")
                        TDFileUtils.replaceProp(it, "initWithCallback(context, env, null, fmCallback);", "Map<String, Object> options = new HashMap<>();options.put(OPTION_CUSTOM_URL,url);initWithCallback(context, ENV_PRODUCTION, options,fmCallback);")
                        //将initWithCallback由public 变更为private
                        TDFileUtils.replaceProp(it, "public static void initWithCallback(final Context context,", "private static void initWithCallback(final Context context,")
                        TDFileUtils.replaceProp(it, "private static void initWithCallback2(final Context context,", "public static void initWithCallback2(final Context context,")
                    }

                    //删除判断环境
                    def initialFilter_file = new File(FileUtils.join(path, "src/main/java/cn/tongdun/android/shell/common", "InitialFilter.java"))
                    TDFileUtils.replaceProp(initialFilter_file, "if (proxyUrl == null && (env == null || (!env.equals(FMAgent.ENV_PRODUCTION) && !env.equals(FMAgent.ENV_SANDBOX)))) {", "if(false){")
                    //打开数据迁移
                    def tdCookieIDHelper_file = new File(FileUtils.join(path, "src/main/java/cn/tongdun/android/core/common", "CookieIDHelper.java"))
                    TDFileUtils.replaceProp(tdCookieIDHelper_file, "// dataMigration(context);", "dataMigration(context);")
                    println TAG + "> end build enterprise"
                }
            }
    }
    /**
     * 生成混淆字典
     */
    def generation_dictt = {
        DictionaryGenerator.generatorDicFile(new File("./proguard-dict.txt"))
        println "> Generation dict"
    }
    /**
     * 字符串混淆及log移除
     */
    def runScript = {
        ProcessorConfig config ->
            if (!config.logSwitch) {
                //移除java层log
                TDFileUtils.removeJavaLog('./sdk-shell/src/main/java')
            }
            //native层name混淆
            ObfuscateGenerator.generateObNativeName('./sdk-shell/src/main/jni')
    }
    /**
     * 编译module
     */
    def build_module = {
        ProcessorConfig config ->
            def rootProject = ProjectUtils.getRootProject.call() as Project
            def taskBuildType = config.buildType == Constant.BUILD_RELEASE ? 'syncReleaseLibJars' : 'syncDebugLibJars'
            ProjectUtils.gradleExec(rootProject, "./gradlew -q clean")
            println "${TAG} Clean Project"

            Thread.start {
                buildOptionModule(rootProject, taskBuildType)
                buildSdkShell(rootProject, taskBuildType)
                mLatch.countDown()
            }
            buildNdk(rootProject, config)
            boolean isWait = mLatch.await(5L, TimeUnit.MINUTES)
            println "${TAG} > wait for sdkshell build:${isWait}"
            if (!isWait) {
                throw new GradleException("the buildSdkShell build timeout!")
            }

    }
    /**
     * 编译sdk-shell
     */
    def buildSdkShell = { Project rootProject, def taskBuildType ->
        rootProject.with {
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_COMMON}:${taskBuildType}")
            def copyCommonFile = 'copyCommonFile'
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_SHELL}:${taskBuildType} ${copyCommonFile}")
            println "${TAG} > Build module of sdk-shell"
        }
    }
    /**
     * 可选的插件包
     */
    def buildOptionModule = { Project rootProject, def taskBuildType ->
        rootProject.with {
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_PACKAGELIST}:${taskBuildType}")
            println "${TAG}> Build module of sdk-packagelist"
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_READPHONE}:${taskBuildType}")
            println "${TAG} > Build module of sdk-readphone"
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_WIFIINFO}:${taskBuildType}")
            println "${TAG} > Build module of sdk-wifiinfo"
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_SENSOR}:${taskBuildType}")
            println "${TAG} > Build module of sdk-sensor"
            ProjectUtils.gradleExec(it, "./gradlew -q :${Constant.MODULE_SDK_LOCATION}:${taskBuildType}")
            println "${TAG} > Build module of sdk-location"
        }
    }
    /**
     * 编译sdk-shell ndk
     */
    def buildNdk = { Project rootProject, ProcessorConfig config ->
        (ProjectUtils.getProjectByName(Constant.MODULE_SDK_SHELL) as Project).tap {
            //1.删除libs和obj中的文件
            getProjectDir().getPath().with {
                new File(FileUtils.join(it, "src", "main", "libs")).deleteDir()
                new File(FileUtils.join(it, "src", "main", "obj")).deleteDir()
            }
        }
        //2.ndk编译
        def usesmalg = "False"
        def ndk_enterprice = "FALSE"
        if (config.sdkType == Constant.SDK_PAAS) {
            usesmalg = "USE_SM"
            ndk_enterprice = "True"
        }
        rootProject.with {
            if (config.buildType == Constant.BUILD_RELEASE) {
                ProjectUtils.gradleExec(it, "cd sdk-shell/src/main/jni/ && ndk-build mode=RELEASE alg=${usesmalg} enter=${ndk_enterprice} ollvm=True")
            } else {
                ProjectUtils.gradleExec(it, "cd sdk-shell/src/main/jni/ && ndk-build mode=DEBUG alg=${usesmalg} enter=${ndk_enterprice}")
            }
            println "$TAG > Build module of sdk-shell ndk-library"
        }
    }
}