package com.bobryshev.testinnovec

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime

class MainActivityViewModel: ViewModel() {

    var isNetworkAvailable: Boolean = true

    private val actionList: MutableList<ActionData> = mutableListOf()
    val actionsLiveData: MutableLiveData<Action> = MutableLiveData()

    fun clickButton() {
        checkConditions()
    }

    private fun checkConditions() {
        actionList.sortedByDescending { it.priority }
            .forEach { data ->
                if (data.checkConditions()) {
                    val action = getActionFromActionData(data)
                    if (action == Action.TOAST && isNetworkAvailable.not()) {
                        return
                    }
                    actionsLiveData.postValue(action)
                    data.lastExecute = DateTime.now().millis
                    return
                }
            }
    }

    fun convertString(value: String) {
        val sType = object : TypeToken<List<ActionData>>() { }.type
        actionList.addAll(Gson().fromJson<List<ActionData>>(value, sType))
    }
}