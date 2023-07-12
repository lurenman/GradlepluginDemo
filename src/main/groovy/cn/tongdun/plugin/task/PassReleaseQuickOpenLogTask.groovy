package cn.tongdun.plugin.task

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.processor.BasePackSdkProcess
import cn.tongdun.plugin.processor.ProcessorChain
import cn.tongdun.plugin.processor.ProcessorConfig
import org.gradle.api.tasks.TaskAction

class PassReleaseQuickOpenLogTask extends TDBaseTask {
    @TaskAction
    void action() {
        super.action()
    }

    @Override
    def setConfigParameter(ProcessorConfig config) {
        config.tap {
            buildType = Constant.BUILD_RELEASE
            logSwitch = true
            sdkType = Constant.SDK_PAAS
        }
    }

    @Override
    protected lazyInitChain() {
        new ProcessorChain().tap {
            addProcessor(checkProcess)
            addProcessor(buildModuleProcess)
            addProcessor(new BasePackSdkProcess())
        }
    }

    def methodMissing(String name, Object args) {
        println("Missing the method is ${name} the params is ${args}")
    }
}
