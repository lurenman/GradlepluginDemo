package cn.tongdun.plugin;


import org.objectweb.asm.MethodVisitor;

public interface AppkeyMethodTarget {
    public boolean visitMethod(MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions);
}
