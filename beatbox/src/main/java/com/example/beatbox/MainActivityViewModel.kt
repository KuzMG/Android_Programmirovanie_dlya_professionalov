package com.example.beatbox

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var beatBox: BeatBox
        get() = _beatBox!!
        set(value) {
            _beatBox = value
        }
    private var _beatBox: BeatBox? = null
    val titleRateLiveData = MutableLiveData<String>()


    fun setRate(value: Int) {
        beatBox.rate = value / 100f
        titleRateLiveData.postValue(value.toString())
    }

    override fun onCleared() {
        super.onCleared()
        beatBox.realese()
    }
}