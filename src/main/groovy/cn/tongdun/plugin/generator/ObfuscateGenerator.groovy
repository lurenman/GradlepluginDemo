package cn.tongdun.plugin.generator


import groovy.io.FileType

class ObfuscateGenerator {
    private static def funcDicts = [:]
    /**
     * 混淆native name
     */
    static def generateObNativeName = { def path ->
        funcDicts.clear()
        def pattern = ~/FIXNAME\(\"(.*)\"\)/
        new File(path).eachFileRecurse(FileType.FILES) { desfile ->
            if (desfile.name.endsWith('.h')) {
                def lines = desfile.readLines()
                def findFIXNAME = false
                lines.eachWithIndex { String it, int index ->
                    def match = pattern.matcher(it)
                    if (match) {
                        findFIXNAME = true
                        def srcFuncName = match.group(1)
                        def encStrName = generateFuncName.call()
                        funcDicts[srcFuncName] = encStrName
                        lines.set(index, match.replaceAll("FIXNAME(\"${encStrName}\")"))
                    }
                }
                if (findFIXNAME) {
                    desfile.withWriter { writer ->
                        lines.each { line ->
                            writer.append(line + "\r\n")
                        }
                    }
                }

            }
        }
        if (funcDicts.size() > 0) {
            fixHookdata()
            nativeFunNameToFile.call()
        }
    }
    /**
     * 修改TdHook.c对应的方法name，如果代码不换行可以遍历group操作
     * @return
     */
    private static def fixHookdata() {
        def file = new File("./sdk-shell/src/main/jni/TdHook.c")
        def lines = file.readLines()
        def pattern = ~/\{"(.*)"/
        def findIndic = false
        lines.eachWithIndex { String it, int index ->
            def match = pattern.matcher(it)
            if (match) {
                def srcName = match.group(1)
                if (funcDicts.containsKey(srcName)) {
                    findIndic = true
                    def destname = funcDicts[srcName]
                    lines.set(index, match.replaceFirst("{\"${destname}\""))
                }
            }
        }
        if (findIndic) {
            file.withWriter { writer ->
                lines.each { line ->
                    writer.append(line + "\r\n")
                }
            }
        }
    }
    /**
     * fun name->file
     */
    private static def nativeFunNameToFile = {
        def file = new File('./nativeNameMap.txt')
        file.withWriter {
            writer ->
                funcDicts.each {
                    def key = it.getKey()
                    def value = it.getValue()
                    writer.append(key + ":" + value + "\r\n")
                }
        }
    }

    /**
     * 生成随机拼接字符大小写字母
     */
    static def generateFuncName = {
        def generator = { String alphabet, int maxlen, int mixlen ->
            new Random().with {
                (0..(it.nextInt(maxlen - mixlen + 1) + mixlen)).collect { alphabet[nextInt(alphabet.length())] }.join()
            }
        }
        generator((('A'..'Z') + ('a'..'z')).join(), 32, 16)
    }
}