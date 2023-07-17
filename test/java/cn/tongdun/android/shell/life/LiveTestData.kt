package cn.tongdun.android.shell.life


/**
 * @Author yang.bai.
 * Date: 2022/12/5
 */
class LiveTestData<T> {

    private val mObservers: LinkedHashMap<Observer<in T>, LifecycleBoundObserver> = LinkedHashMap()

    private var mData: T? = null

    constructor() {

    }

    constructor(mData: T?) {
        this.mData = mData
    }

    /**
     * owner中添加观察者
     */
    fun observe(owner: LifeTestcycleOwner, observer: Observer<in T>) {
        if (owner.getLifecycle().getCurrentState() == LifeTestcycle.State.DESTROYED) {
            // ignore
            return
        }
        val wrapper = LifecycleBoundObserver(owner, observer)
        val existing = mObservers.put(observer, wrapper)
        require(!(existing != null && !existing.isAttachedTo(owner))) {
            ("Cannot add the same observer"
                    + " with different lifecycles")
        }
        if (existing != null) {
            return
        }
        owner.getLifecycle().addObserver(wrapper)
    }

    /**
     * owner中移除观察者
     */
    fun removeObserver(observer: Observer<in T>) {
        mObservers.remove(observer)
    }

    fun setValue(value: T) {
        mData = value
        dispatchingValue(null)
    }

    fun dispatchingValue(initiator: LifecycleBoundObserver?) {
        if (initiator != null) {
            initiator.run {
                mObserver.onChanged(mData)
            }
        } else {
            //通知所有观察者
            val iterator = mObservers.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next()
                value.mObserver.onChanged(mData)
            }
        }
    }

    /**
     * 实际Observer装饰类
     */
    inner class LifecycleBoundObserver(
        val mOwner: LifeTestcycleOwner,
        val mObserver: Observer<in T>
    ) : LifeTestcycleRegistry.LifecycleEventObserver {

        fun shouldBeActive(): Boolean {
            return true
        }

        fun isAttachedTo(owner: LifeTestcycleOwner): Boolean {
            return mOwner === owner
        }

        fun detachObserver() {
            mOwner.getLifecycle().removeObserver(this)
        }

        override fun onStateChanged(source: LifeTestcycleOwner, event: LifeTestcycle.Event) {
            val currentState = mOwner.getLifecycle().getCurrentState()
            if (currentState == LifeTestcycle.State.DESTROYED || currentState == LifeTestcycle.State.STOP) {
                this@LiveTestData.removeObserver(mObserver)
                return
            }
            //不需要恢复周期调用功能
            //this@LiveTestData.dispatchingValue(this)
        }
    }
}