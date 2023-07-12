package cn.tongdun.plugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Set;

import cn.tongdun.plugin.model.DeviceInfoField;
import cn.tongdun.plugin.utils.ScanSetting;
import cn.tongdun.plugin.utils.StringEncryptionUtil;

/**
 * 方法修改
 * AdviceAdapter API：https://asm.ow2.io/javadoc/org/objectweb/asm/commons/AdviceAdapter.html
 * AdviceAdapter：实现了MethodVisitor接口，主要访问方法的信息。用来对具体方法进行字节码操作；
 * FieldVisitor：访问具体的类成员；
 * AnnotationVisitor：访问具体的注解信息
 */
public class ScanMethodAdapter extends AdviceAdapter {

    private final ScanClassVisitor mScanClassVisitor;
    private final String mMethodName;

    protected ScanMethodAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, ScanClassVisitor scanClassVisitor) {
        super(api, methodVisitor, access, name, descriptor);
        mScanClassVisitor = scanClassVisitor;
        mMethodName = name;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        if (ScanSetting.CLINIT_METHOD_NAME.equals(mMethodName) && mScanClassVisitor.mStaticFinalField.size() > 0) {
            Set<String> strings = mScanClassVisitor.mStaticFinalField.keySet();
            for (String field : strings) {
                String value = mScanClassVisitor.mStaticFinalField.get(field);
                if ("".equals(value) || value == null) {
                    super.visitLdcInsn(value);
                    continue;
                }
                int key1 = StringEncryptionUtil.getRandomKey();
                String encryption = StringEncryptionUtil.encryption(value, key1, mScanClassVisitor.key2, mScanClassVisitor.key3);
                mv.visitLdcInsn(encryption);
                mv.visitIntInsn(BIPUSH, key1);
                mv.visitMethodInsn(INVOKESTATIC, mScanClassVisitor.mOwner, ScanSetting.STRING_DECRYPT_METHOD_NAME, ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR, false);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, mScanClassVisitor.mOwner, field, ScanSetting.STRING_DESCRIPTOR);
                mScanClassVisitor.hasString = true;
            }
        }
    }

    @Override
    public void visitLdcInsn(Object value) {
        if (value instanceof String && !"".equals(value)) {
            int key1 = StringEncryptionUtil.getRandomKey();
            String encryption = StringEncryptionUtil.encryption((String) value, key1, mScanClassVisitor.key2, mScanClassVisitor.key3);
            mv.visitLdcInsn(encryption);
            mv.visitIntInsn(BIPUSH, key1);
            mv.visitMethodInsn(INVOKESTATIC, mScanClassVisitor.mOwner, ScanSetting.STRING_DECRYPT_METHOD_NAME, ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR, false);
            mScanClassVisitor.hasString = true;
        } else {
            super.visitLdcInsn(value);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        // release包去掉log打印
//        if (mScanClassVisitor.mConfig.isRelease() && checkLogUtilMethodName(name)
//                && "cn/tongdun/android/common/util/LogUtil".equals(owner)) {
//            return;
//        }
        //企业级去掉initBugly
//        if (mScanClassVisitor.mConfig.isEnterprise() && "initBugly".equals(name)
//                && "cn/tongdun/android/core/FMCore".equals(owner) && "(Landroid/content/Context;)V".equals(descriptor)) {
//            return;
//        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        // 如果是DeviceInfo类的构造方法
        if (ScanSetting.DEVICE_INFO_CLASS_OWNER.equals(mScanClassVisitor.mOwner) && ScanSetting.INIT_METHOD_NAME.equals(mMethodName)) {
            insertGetMethodAdapter();
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void endMethod() {
        super.endMethod();
    }

    private void insertGetMethodAdapter() {
        for (DeviceInfoField field : mScanClassVisitor.deviceInfoField) {
            if (field == null) {
                continue;
            }
            Label label1 = new Label();
            mv.visitLabel(label1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, ScanSetting.DEVICE_INFO_CLASS_OWNER, ScanSetting.DEVICE_INFO_GET_METHOD_PREFIX + field.getName(), "()"+field.getDescriptor(), false);
            mv.visitInsn(POP);
        }
    }
}
