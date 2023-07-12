package cn.tongdun.plugin.task

import cn.tongdun.plugin.Constant
import cn.tongdun.plugin.processor.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class TDBaseTask extends DefaultTask {
    @Internal def TAG = "TDBaseTask >"
    protected def config
    protected def processorChain
    protected def checkProcess
    protected def buildModuleProcess
    protected def packSdkProcess

    TDBaseTask() {
        group Constant.GROUP_NAME
        config = new ProcessorConfig()
        checkProcess = new CheckPreProcess()
        buildModuleProcess = new BuildModuleProcess()
        packSdkProcess = new PackSdkProcess(new BasePackSdkProcess())
    }

    @TaskAction
    void action() {
        setConfigParameter(config)
        def bProcess = processorChain = lazyInitChain().with {
            process(config, it)
        }

        println "$TAG taskName ${this.name} processorChain process :$bProcess"
    }

    abstract def setConfigParameter(ProcessorConfig config)

    protected def lazyInitChain() {
        new ProcessorChain().tap {
            addProcessor(checkProcess)
            addProcessor(buildModuleProcess)
            addProcessor(packSdkProcess)
        }
    }
}
