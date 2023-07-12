package cn.tongdun.plugin.processor

trait Processor<T> {
    abstract boolean handleProcess(T data, ProcessorChain<T> chain)
}