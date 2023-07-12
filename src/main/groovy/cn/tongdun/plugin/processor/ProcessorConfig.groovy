package cn.tongdun.plugin.processor

import cn.tongdun.plugin.Constant

/**
 * chain处理读取设置配置
 */
class ProcessorConfig {
    //打包类型release or debug
    def buildType = Constant.BUILD_RELEASE
    //log日志开关
    def logSwitch = false
    //sdk paas or saas
    def sdkType = Constant.SDK_SAAS
    //sdk版本
    def sdkVersion

    //编译版本
    def compileSdkVersion
    //编译版本android.jar path
    def compileSdkVersionJarPath
    //ollvm
    def ollvmPath = "darwin-x86_64"
    //md5
    def sha256Cmd = "shasum -a 256"
    //jni.srcDirs
    def jniSrcDirs
    //系统平台
    def host_os
}