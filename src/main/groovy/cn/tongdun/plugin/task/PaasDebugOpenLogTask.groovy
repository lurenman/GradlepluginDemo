package cn.tongdun.plugin.task

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.processor.ProcessorConfig
import org.gradle.api.tasks.TaskAction

class PaasDebugOpenLogTask extends TDBaseTask {
    @TaskAction
    void action() {
        super.action()
    }

    @Override
    def setConfigParameter(ProcessorConfig config) {
        config.tap {
            buildType = Constant.BUILD_DEBUG
            logSwitch = true
            sdkType = Constant.SDK_PAAS
        }
    }

    def methodMissing(String name, Object args) {
        println("Missing the method is ${name} the params is ${args}")
    }
}
