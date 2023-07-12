package cn.tongdun.plugin.task

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.processor.ProcessorConfig
import org.gradle.api.tasks.TaskAction

class SaasReleaseOpenLogTask extends TDBaseTask {
    @TaskAction
    void action() {
        super.action()
    }

    @Override
    def setConfigParameter(ProcessorConfig config) {
        config.tap {
            buildType = Constant.BUILD_RELEASE
            logSwitch = true
            sdkType = Constant.SDK_SAAS
        }
    }

    def methodMissing(String name, Object args) {
        println("Missing the method is ${name} the params is ${args}")
    }
}
