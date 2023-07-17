package cn.tongdun.android.shell

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * @Author yang.bai.
 * Date: 2022/11/25
 */
//@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class RobolectricTest {
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }

}