package com.test.coinapp.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class SingleEmitLiveData<T>: MutableLiveData<T>() {
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        removeObservers(owner)
        val selfData = this
        super.observe(owner, Observer<T> { t ->
            if (t != null) {
                observer.onChanged(t)
                selfData.value = null
            }
        })
    }
}