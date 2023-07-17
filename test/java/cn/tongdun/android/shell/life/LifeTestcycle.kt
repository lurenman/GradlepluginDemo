package cn.tongdun.android.shell.life

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleObserver

/**
 * @Author yang.bai.
 * Date: 2022/12/5
 */
abstract class LifeTestcycle {
    @MainThread
    abstract fun addObserver(observer: LifecycleObserver)

    abstract fun removeObserver(observer: LifecycleObserver)

    abstract fun getCurrentState(): State

    enum class Event {
        ON_CREATE,
        ON_RUN,
        ON_STOP,
        ON_DESTROY;

        open fun getTargetState(): State {
            when (this) {
                ON_CREATE -> return State.CREATED
                ON_RUN -> return State.RUN
                ON_STOP -> return State.STOP
                ON_DESTROY -> return State.DESTROYED
            }
        }
    }


    enum class State {
        INITIALIZED,
        CREATED,
        RUN,
        STOP,
        DESTROYED;


        fun isAtLeast(state: State): Boolean {
            return compareTo(state) >= 0
        }
    }
}