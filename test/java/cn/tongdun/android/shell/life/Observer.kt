package cn.tongdun.android.shell.life

/**
 * @Author yang.bai.
 * Date: 2022/12/6
 */
interface Observer<T> {
    fun onChanged(value: T?)
}