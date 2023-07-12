package cn.tongdun.plugin.processor

class ProcessorChain<T> {
    // 保存处理节点
    def processorList = new ArrayList<>()
    // 处理节点下标
    def index = 0
    // 动态扩展处理节点
    def addProcessor(Processor<T> processor) {
        processorList.add(processor)
        processor
    }
    //重制index，要保证同一个thread
    def resetIndex() {
        index = 0
    }
    // 获取处理器处理
    def process(T data, ProcessorChain<T> chain) {
        if (index == processorList.size()) {
            return
        }
        Processor<T> processor = processorList.get(index)
        index++
        processor.handleProcess(data, chain)
    }
}