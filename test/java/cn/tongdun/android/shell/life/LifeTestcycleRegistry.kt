package cn.tongdun.android.shell.life

import androidx.lifecycle.LifecycleObserver
import java.lang.ref.WeakReference

/**
 * @Author yang.bai.
 * Date: 2022/12/5
 */
class LifeTestcycleRegistry() : LifeTestcycle() {
    private val mObserverMap: LinkedHashMap<LifecycleObserver, ObserverWithState> = LinkedHashMap()
    private var mState: State? = null
    private var mLifecycleOwner: WeakReference<LifeTestcycleOwner>? = null

    constructor(provider: LifeTestcycleOwner) : this() {
        mLifecycleOwner = WeakReference(provider)
        mState = State.INITIALIZED
    }

    fun setCurrentState(state: State) {
        moveToState(state)
    }

    fun handleLifecycleEvent(event: Event) {
        moveToState(event.getTargetState())
        val lifecycleOwner = mLifecycleOwner?.get() ?: return
        val iterator = mObserverMap.entries.iterator()
        while (iterator.hasNext()) {
            val (_, value) = iterator.next()
            value.dispatchEvent(lifecycleOwner, event)
            if (event == Event.ON_STOP)
                iterator.remove()
        }
    }

    private fun moveToState(next: State) {
        if (mState == next) {
            return
        }
        mState = next
    }

    override fun addObserver(observer: LifecycleObserver) {
        mLifecycleOwner?.get() ?: return
        val initialState = if (mState == State.DESTROYED) State.DESTROYED else State.INITIALIZED
        val exit = mObserverMap.put(observer, ObserverWithState(observer, initialState))
        if (exit != null) {
            return
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        mObserverMap.remove(observer)
    }

    override fun getCurrentState(): State {
        return mState!!
    }

    fun getObserverCount(): Int {
        return mObserverMap.size
    }

    internal class ObserverWithState(
        observer: LifecycleObserver,
        initialState: State
    ) {
        var mState: State
        var mLifecycleObserver: LifecycleEventObserver

        fun dispatchEvent(owner: LifeTestcycleOwner, event: Event) {
            val newState: State = event.getTargetState()
            mState = newState
            mLifecycleObserver.onStateChanged(owner, event)
        }

        init {
            mLifecycleObserver = observer as LifecycleEventObserver
            mState = initialState
        }
    }

    interface LifecycleEventObserver : LifecycleObserver {
        fun onStateChanged(source: LifeTestcycleOwner, event: Event)
    }
}