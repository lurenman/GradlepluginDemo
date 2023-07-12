package cn.tongdun.plugin.factory

import cn.tongdun.plugin.task.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskContainer

class TaskFactory {

    static def void registerTasks(TaskContainer taskContainer) {
        [:].tap {
            put("saasRelease", SaasReleaseTask.class)
            put("saasReleaseOpenLog", SaasReleaseOpenLogTask.class)
            put("saasDebug", SaasDebugTask.class)
            put("saasDebugOpenLog", SaasDebugOpenLogTask.class)
            put("saasReleaseQuick", SaasReleaseQuickTask.class)
            put("saasReleaseOpenLogQuick", SaasReleaseQuickOpenLogTask.class)
            put("paasRelease", PaasReleaseTask.class)
            put("paasReleaseOpenLog", PaasReleaseOpenLogTask.class)
            put("paasDebug", PaasDebugTask.class)
            put("paasDebugOpenLog", PaasDebugOpenLogTask.class)
            put("passReleaseQuick", PassReleaseQuickTask.class)
            put("passReleaseOpenLogQuick", PassReleaseQuickOpenLogTask.class)
        }.each {
            createTask(taskContainer, it.key, it.value)
        }
    }

    static def <T extends DefaultTask> T createTask(TaskContainer taskContainer, String taskName, final Class<T> aClass) {
        taskContainer.with {
            create(taskName, aClass)
        }
    }
}