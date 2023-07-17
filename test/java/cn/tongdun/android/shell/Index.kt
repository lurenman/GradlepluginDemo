package cn.tongdun.android.shell

/**
 * @Author yang.bai.
 * Date: 2022/12/2
 * 主要做个标记的作用
 * 1-3代表1到3的字段
 * 1*3代表1字段和3字段
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Index(val value: Int)


