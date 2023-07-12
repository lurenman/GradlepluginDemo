package cn.tongdun.plugin;


import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ACC_PRIVATE;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ACC_STATIC;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ALOAD;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ARETURN;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ARRAYLENGTH;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ASTORE;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.BALOAD;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.BASTORE;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.CALOAD;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.DUP;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.GOTO;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.I2B;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IADD;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ICONST_0;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ICONST_1;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ICONST_2;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ICONST_4;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IDIV;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IF_ICMPGE;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ILOAD;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IMUL;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.INVOKESPECIAL;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.INVOKEVIRTUAL;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IOR;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ISHL;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ISTORE;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IXOR;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.NEW;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.NEWARRAY;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.SIPUSH;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.T_BYTE;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tongdun.plugin.model.DeviceInfoField;
import cn.tongdun.plugin.utils.ScanSetting;
import cn.tongdun.plugin.utils.StringEncryptionUtil;

/**
 * ClassVisitor API: https://asm.ow2.io/javadoc/org/objectweb/asm/ClassVisitor.html
 */
public class ScanClassVisitor extends ClassVisitor {

    private static final int ACC_STATIC_FINAL = Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
    public HashMap<String, String> mStaticFinalField = new HashMap<>();
    protected boolean hasClinit;
    /**
     * 存在全局字符串变量
     */
    public boolean hasString;
    /**
     * 扩展选项
     */
    protected PluginConfig mConfig;
    /**
     * 是否已经有StringDecrypt方法
     */
    protected boolean hasStringDecrypt;
    protected String mOwner;
    // 同一个类里面 key2和key3是固定的，不同类key2和key3不一样
    public int key2, key3;
    //记录DeviceInfo中的所有成员变量，用于生成get方法和在初始化方法中调用get方法
    public List<DeviceInfoField> deviceInfoField = new ArrayList<>();

    public ScanClassVisitor(int api, ClassVisitor classVisitor, PluginConfig config) {
        super(api, classVisitor);
        mConfig = config;
        key2 = StringEncryptionUtil.getRandomKey();
        key3 = StringEncryptionUtil.getRandomKey();
    }

    /**
     * 可以拿到类的详细信息，然后对满足条件的类进行过滤
     *
     * @param version    JDK版本
     * @param access     类的修饰符
     * @param name       类的名称，通常使用完整包名+类名
     * @param signature  泛型信息，如果没有定义泛型该参数为null
     * @param superName  当前类的父类，所有类的父类java.lang.Object
     * @param interfaces 实现的接口列表
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mOwner = name;
    }

    /**
     * 访问内部类信息
     *
     * @param name      内部类名称
     * @param outerName 内部类所属的类的名称
     * @param innerName 内部类在其封闭类中的（简单）名称。对于匿名内部类，可能为 null。
     * @param access    内部类的修饰符
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    /**
     * 类中字段
     *
     * @param access     修饰符
     * @param name       字段名
     * @param descriptor 字段类型
     * @param signature  泛型描述
     * @param value      默认值
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {

        if (!mConfig.isOpenLog() && "cn/tongdun/android/common/util/LogUtil".equals(mOwner) && "DEV".equals(name) && "Z".equals(descriptor)) {
            System.out.println("--- Tongdun Plugin LogUtil DEV = false");
            return super.visitField(access, name, descriptor, signature, 0);
        }
        // 如果是DeviceInfo，记录所有的字段。方便后续在构造方法中调用get方法
        if (ScanSetting.DEVICE_INFO_CLASS_OWNER.equals(mOwner)) {
            deviceInfoField.add(new DeviceInfoField(name, descriptor, signature));
        }
        boolean isStaticFinal = (access & ACC_STATIC_FINAL) == ACC_STATIC_FINAL;
        if (value instanceof String && !"".equals(value)) {
            hasString = true;
            // 将 final + static 修饰的变量置空（不然无法在静态块中初始化），之后再在<clinit>中赋值
            if (ScanSetting.STRING_DESCRIPTOR.equals(descriptor) && isStaticFinal) {
                mStaticFinalField.put(name, (String) value);
                return super.visitField(access, name, descriptor, signature, null);
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    /**
     * 拿到要修改的方法，然后进行修改
     *
     * @param access     方法的修饰符
     * @param name       方法名
     * @param descriptor 方法签名，返回值
     * @param signature  泛型相关信息
     * @param exceptions 抛出的异常，没有异常抛出该参数为null
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (ScanSetting.STRING_DECRYPT_METHOD_NAME.equals(name) && ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR.equals(descriptor)) {
            hasStringDecrypt = true;
            return methodVisitor;
        }
        if (ScanSetting.CLINIT_METHOD_NAME.equals(name)) {
            hasClinit = true;
        }
        return new ScanMethodAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor, this);
    }

    /**
     * 该方法是最后一个被调用的方法，用于通知访问者该类的所有字段和方法都已访问。
     */
    @Override
    public void visitEnd() {

        // 自动为DeviceInfo中的成员变量增加getInfoBy+name 方法
        if (ScanSetting.DEVICE_INFO_CLASS_OWNER.equals(mOwner) && deviceInfoField.size() > 0) {
            for (DeviceInfoField field : deviceInfoField) {
                if (field == null) {
                    continue;
                }
                addGetInfoMethod(field);
            }
        }

        // 如果没有扫描<clinit>方法，则说明全是final + static；需要插入static <clinit>()方法
        if (!hasClinit && mStaticFinalField.size() > 0) {
            MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC, ScanSetting.CLINIT_METHOD_NAME, "()V", null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }
        if (hasString && !hasStringDecrypt) {
            addStringDecryptMethod(key2, key3);
        }
        super.visitEnd();
    }

    private void addGetInfoMethod(DeviceInfoField deviceInfoField) {
        MethodVisitor methodVisitor = this.visitMethod(Opcodes.ACC_PRIVATE, ScanSetting.DEVICE_INFO_GET_METHOD_PREFIX + deviceInfoField.getName(), "()" + deviceInfoField.getDescriptor(), "()" + deviceInfoField.getSignature(), null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "cn/tongdun/android/core/model/DeviceInfo", deviceInfoField.getName(), deviceInfoField.getDescriptor());
        methodVisitor.visitInsn(ARETURN);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLocalVariable("this", "Lcn/tongdun/android/core/model/DeviceInfo;", null, label0, label1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    /**
     * ScanClassVisitor设置 ClassReader.EXPAND_FRAMES 使用
     *
     * @param key2
     * @param key3
     */
    private void addStringDecryptMethod(int key2, int key3) {
        MethodVisitor methodVisitor = this.visitMethod(ACC_PRIVATE | ACC_STATIC, ScanSetting.STRING_DECRYPT_METHOD_NAME, ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR, null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IDIV);
        methodVisitor.visitVarInsn(ISTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        methodVisitor.visitVarInsn(ASTORE, 3);
        methodVisitor.visitVarInsn(ILOAD, 2);
        methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
        methodVisitor.visitVarInsn(ASTORE, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, 5);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitFrame(Opcodes.F_NEW, 6, new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[B", Opcodes.INTEGER}, 0, new Object[]{});
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitVarInsn(ILOAD, 2);
        Label label4 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IMUL);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitInsn(ISHL);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(IOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitIincInsn(5, 1);
        methodVisitor.visitJumpInsn(GOTO, label3);
        methodVisitor.visitLabel(label4);
        methodVisitor.visitFrame(Opcodes.F_NEW, 5, new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[B"}, 0, new Object[]{});
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitIntInsn(SIPUSH, key2);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitVarInsn(ISTORE, 5);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitIntInsn(SIPUSH, key3);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ISTORE, 7);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitFrame(Opcodes.F_NEW, 8, new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[B", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[]{});
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ARRAYLENGTH);
        Label label6 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label6);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitIincInsn(7, 1);
        methodVisitor.visitJumpInsn(GOTO, label5);
        methodVisitor.visitLabel(label6);
        methodVisitor.visitFrame(Opcodes.F_NEW, 7, new Object[]{"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", "[B", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[]{});
        methodVisitor.visitTypeInsn(NEW, "java/lang/String");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn("UTF-8");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/lang/String;)V", false);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[]{"java/lang/String", Opcodes.INTEGER}, 1, new Object[]{"java/lang/Exception"});
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
        methodVisitor.visitLdcInsn("");
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(7, 8);
        methodVisitor.visitEnd();
    }

    /**
     * ScanClassVisitor设置 ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG 使用
     *
     * @param key2
     * @param key3
     */
    private void addStringDecryptMethod2(int key2, int key3) {
        MethodVisitor methodVisitor = this.visitMethod(ACC_PRIVATE | ACC_STATIC, ScanSetting.STRING_DECRYPT_METHOD_NAME, ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR, null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        Label label1 = new Label();
        Label label2 = new Label();
        methodVisitor.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
        methodVisitor.visitLabel(label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IDIV);
        methodVisitor.visitVarInsn(ISTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        methodVisitor.visitVarInsn(ASTORE, 3);
        methodVisitor.visitVarInsn(ILOAD, 2);
        methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
        methodVisitor.visitVarInsn(ASTORE, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, 5);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitVarInsn(ILOAD, 2);
        Label label4 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(ICONST_2);
        methodVisitor.visitInsn(IMUL);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitInsn(ISHL);
        methodVisitor.visitLdcInsn("0123456789abcdef");
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IADD);
        methodVisitor.visitInsn(CALOAD);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I", false);
        methodVisitor.visitInsn(IOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitIincInsn(5, 1);
        methodVisitor.visitJumpInsn(GOTO, label3);
        methodVisitor.visitLabel(label4);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitIntInsn(SIPUSH, key2);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitVarInsn(ISTORE, 5);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitIntInsn(SIPUSH, key3);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitVarInsn(ISTORE, 7);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitInsn(ARRAYLENGTH);
        Label label6 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label6);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ILOAD, 6);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitInsn(IXOR);
        methodVisitor.visitInsn(I2B);
        methodVisitor.visitInsn(BASTORE);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 7);
        methodVisitor.visitInsn(BALOAD);
        methodVisitor.visitVarInsn(ISTORE, 6);
        methodVisitor.visitIincInsn(7, 1);
        methodVisitor.visitJumpInsn(GOTO, label5);
        methodVisitor.visitLabel(label6);
        methodVisitor.visitTypeInsn(NEW, "java/lang/String");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitLdcInsn("UTF-8");
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/lang/String;)V", false);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitVarInsn(ASTORE, 2);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
        methodVisitor.visitLdcInsn("");
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(7, 8);
        methodVisitor.visitEnd();
    }
}
