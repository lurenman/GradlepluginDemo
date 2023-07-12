package cn.tongdun.plugin;

import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ACONST_NULL;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ARETURN;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.GETSTATIC;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.ICONST_0;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.IRETURN;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.LCONST_0;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.LRETURN;
import static org.gradle.internal.impldep.bsh.org.objectweb.asm.Constants.RETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.tongdun.plugin.utils.ScanSetting;

/**
 * 适配清除AppKeyUtils相关方法的验证
 */

public class AppKeyScanClassVisitordapter extends ScanClassVisitor implements AppkeyMethodTarget {

    private final String processOwner = "cn/tongdun/android/core/utils/AppKeyUtils";

    public AppKeyScanClassVisitordapter(int api, ClassVisitor classVisitor, PluginConfig config) {
        super(api, classVisitor, config);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (cv == null) {
            return null;
        }
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if (ScanSetting.STRING_DECRYPT_METHOD_NAME.equals(name) && ScanSetting.STRING_DECRYPT_METHOD_DESCRIPTOR.equals(descriptor)) {
            hasStringDecrypt = true;
            return methodVisitor;
        }
        if (ScanSetting.CLINIT_METHOD_NAME.equals(name)) {
            hasClinit = true;
        }
        if (!mConfig.isAppKeyVerify() && processOwner.equals(mOwner)) {
            if (visitMethod(methodVisitor, access, name, descriptor, signature, exceptions))
                return null;
        }
        return new ScanMethodAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor, this);
    }

    @Override
    public boolean visitMethod(MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("AppKeyScanClassVisitordapter process:" + name);
        if (!("<init>".equals(name)) && !("getInstance".equals(name))) {
            if ("getAppKeyStatus".equals(name)) {
                methodVisitor.visitFieldInsn(GETSTATIC, "cn/tongdun/android/core/utils/AppKeyUtils$AppKeyStatus", "APP_KEY_NORMAL", "Lcn/tongdun/android/core/utils/AppKeyUtils$AppKeyStatus;");
                methodVisitor.visitInsn(ARETURN);
                methodVisitor.visitMaxs(1, 3);
                methodVisitor.visitEnd();
            } else {
                clearMethodBody(methodVisitor, access, name, descriptor);
            }
            return true;
        }
        return false;
    }

    /**
     * 只是针对AppKeyUtils做的特殊清空操作，如果新增方法返回类型，需要适配返回值
     *
     * @param mv
     * @param access
     * @param name
     * @param descriptor
     */
    private void clearMethodBody(MethodVisitor mv, int access, String name, String descriptor) {
        Type type = Type.getType(descriptor);
        Type[] argumentsType = type.getArgumentTypes();
        Type returnType = type.getReturnType();
        int stackSize = returnType.getSize();
        boolean isStaticMethod = ((access & Opcodes.ACC_STATIC) != 0);
        int localSize = isStaticMethod ? 0 : 1;
        for (Type argType : argumentsType) {
            localSize += argType.getSize();
        }
        mv.visitCode();
        if (returnType.getSort() == Type.VOID) {
            mv.visitInsn(RETURN);
        } else if (returnType.getSort() == Type.BOOLEAN) {
            mv.visitInsn(returnType.getOpcode(ICONST_0));
            mv.visitInsn(returnType.getOpcode(IRETURN));
        } else if (returnType.getSort() == Type.LONG) {
            mv.visitInsn(LCONST_0);
            mv.visitInsn(LRETURN);
        } else {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(stackSize, localSize);
        mv.visitEnd();
    }
}
