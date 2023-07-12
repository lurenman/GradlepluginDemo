package cn.tongdun.plugin.util


import cn.tongdun.plugin.processor.ProcessorConfig
import groovy.io.FileType

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TDFileUtils {
    static def TAG = 'TDFileUtils >'
    static def final BUFF_SIZE = 1024 * 1024; // 1M Byte
    /**
     * 读取文件中属性
     * @param filename
     * @param key
     * @param seg
     */
    static def readProp =
            {
                File file, key, seg ->
                    def value
                    file.find { line ->
                        if (line.contains(key)) {
                            if (value == null) {
                                value = line.split(seg)[1]
                                return true
                            }
                        }
                    }
                    return value
            }
    /**
     * 替换属性,如果长度contains要提供的值，就return
     */
    static def replaceProp = {
        File file, String oldstr, String newstr ->
            def t = file.getText()
            if (oldstr.length() < newstr.length() && t.contains(newstr))
                return
            t = t.replace(oldstr, newstr)
            file.withWriter {
                writer ->
                    writer.append(t)
            }
    }

    /**
     * 移除文件中的LogUtil代码
     */
    static def removeJavaLog = {
        String path ->
            def patternLog = ~/(LogUtil.i\(|LogUtil.d\(|LogUtil.dev|LogUtil.e\().*/
            new File(path).eachFileRecurse(FileType.FILES) { desfile ->
                def lines = desfile.readLines()
                def tempLines = []
                lines.each {
                    if (patternLog.matcher(it)) {
                        tempLines.add(it)
                    }
                }
                //如果size大于0 说明该文件有log
                if (tempLines.size() > 0) {
                    lines.removeAll(tempLines)
                    desfile.withWriter { writer ->
                        lines.each { line ->
                            writer.append(line + "\r\n")
                        }
                    }
                }
            }
    }
    /**
     * 压缩文件
     */
    static def zipPluginFiles = { Collection<File> resFileList, ZipOutputStream zipout ->
        resFileList.each {
            zipPluginFile(it, zipout, "")
        }
    }
    /**
     * 压缩文件，如果是目录的化以父目录命名
     * @param resFile
     * @param zipout
     * @param rootpath
     * @return
     */
    static def zipPluginFile(File resFile, ZipOutputStream zipout, String rootpath) {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName()
        rootpath = new String(rootpath.getBytes("8859_1"), "GB2312")
        if (resFile.isDirectory()) {
            resFile.listFiles().each { File file ->
                zipPluginFile(file, zipout, file.getParentFile().getName())
            }
        } else {
            def buffer = new byte[BUFF_SIZE]
            def ind = new BufferedInputStream(new FileInputStream(resFile), BUFF_SIZE)
            zipout.with {
                putNextEntry(new ZipEntry(rootpath))
                int realLength
                while ((realLength = ind.read(buffer)) != -1) {
                    write(buffer, 0, realLength)
                }
                ind.close()
                flush()
                closeEntry()
            }
        }
    }

    /**
     * 操作srctmp文件,文件还原等操作
     */
    static def operateSrctmpFile = { ProcessorConfig config, def isCopy ->
        if (isCopy) {
            //复制源代码到srctmp 目录
            println("${TAG} Start copy files to srctmp dir")
            def srctmpFile = new File('./srctmp')
            srctmpFile.with {
                if (exists())
                    srctmpFile.deleteDir()
            }
            ProjectUtils.copy('./sdk-shell/src/main', './srctmp/sdk-shell/src/main')
        } else {
            println("${TAG} Start delete files to srctmp dir and reduction sdk-shell")
            def srctmpFile = new File('./srctmp')
            srctmpFile.with {
                if (exists()) {
                    ProjectUtils.copy('./srctmp/sdk-shell/src/main', './sdk-shell/src/main')
                    deleteDir()
                }
            }
        }
    }
}