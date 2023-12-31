package cn.tongdun.plugin

import cn.tongdun.plugin.utils.ScanSetting
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 字符串加密Transform
 */
class TongdunTransform extends Transform {

    private Project mProject
    /**
     * 扩展选项
     */
    private PluginConfig mConfig

    TongdunTransform(Project project) {
        mProject = project
    }

    /**
     * 获取Transform名称
     * @return transform名称,可以在AS右侧Gradle中找到,路径app/tasks/other/transformClassWithStringObfuscateForDebug
     */
    @Override
    String getName() {
        return "StringEncryption"
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * TransformManager內有多种组合
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * TransformManager內有多种组合
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    /**
     * 是否增量编译
     * 所谓增量编译，是指当源程序的局部发生变更后进重新编译的工作只限于修改的部分及与之相关部分的内容，而不需要对全部代码进行编译
     *
     * @return false：否
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 文档：https://google.github.io/android-gradle-dsl/javadoc/3.4/
     *
     * @param transformInvocation transformInvocation
     * @throws TransformException* @throws InterruptedException* @throws IOException
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println("------------start string encryption transform!------------")
        _transform(transformInvocation.getContext(), transformInvocation.getInputs(), transformInvocation.getOutputProvider(), transformInvocation.isIncremental())
        println("-------------end string encryption transform!-------------")
    }

    /**
     * _transform
     * @param context context
     * @param collectionInput transform输入流，包含两种类型，目录格式和jar
     * @param outputProvider 是用来获取输出目录，我们要将操作后的文件复制到输出目录中。调用getContentLocation方法获取输出目录
     * @param isIncremental 是否增量编译
     * @throws IOException* @throws TransformException* @throws InterruptedException
     */
    private void _transform(Context context, Collection<TransformInput> collectionInput, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        mConfig = mProject.property(ScanSetting.EXTENSIONS_NAME)
        println("--- variantName=" + context.variantName)
        mConfig.release = "release" == context.variantName
        println("--- PluginConfig.enterprise=" + mConfig.enterprise)
        println("--- PluginConfig.release=" + mConfig.release)
        println("--- PluginConfig.openLog=" + mConfig.openLog)
        if (!isIncremental) {
            //非增量,需要删除输出目录
            outputProvider.deleteAll()
        }
        if (collectionInput == null) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }
        if (outputProvider == null) {
            throw new IllegalArgumentException("TransformOutputProvider is null !!!")
        }
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        collectionInput.each { TransformInput transformInput ->
            //对类型为“文件夹”的input进行遍历
            transformInput.directoryInputs.each { DirectoryInput directoryInput ->
                File dir = directoryInput.file
                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)
                if (dir.isDirectory()) {
                    dir.eachFileRecurse(FileType.FILES) { File file ->
                        // scan classes
                        scanClass(file)
                    }
                }
                //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            //jar
            transformInput.jarInputs.each { JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //获取输出路径下的jar包名称；+MD5是为了防止重复打包过程中输出路径名不重复，否则会被覆盖。
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //scan jar file to find classes
                scanJar(jarInput.file)
                //这里执行字节码的注入，不操作字节码的话也要将输入路径拷贝到输出路径
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    private void scanClass(File file) {
        if (!mConfig.release) {
            return
        }
        try {
            ClassReader cr = new ClassReader(file.bytes)
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            ScanClassVisitor sc = new AppKeyScanClassVisitordapter(Opcodes.ASM7, cw, mConfig)
            cr.accept(sc, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG)

            // 写入文件
            byte[] code = cw.toByteArray();
            FileUtils.writeByteArrayToFile(file, code)
        } catch (Exception ignored) {
            println("----scanClass error: file name " + file.getAbsolutePath() + "---")
        }
    }

    private static void scanJar(File jarFile) {
        JarFile file = null;
        try {
            file = new JarFile(jarFile)
            Enumeration<JarEntry> enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            try {
                file.close();
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

}
