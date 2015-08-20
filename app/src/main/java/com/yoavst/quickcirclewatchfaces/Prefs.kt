package com.yoavst.quickcirclewatchfaces

import com.chibatching.kotpref.KotprefModel
import com.yoavst.kotlin.putStringSet
import java.util.HashSet

public object Prefs : KotprefModel() {
    override val kotprefName: String = "prefs"
    var clocks: Set<String>
        get() {
            return kotprefPreference.getStringSet("clocks", emptySet())
        }
        set(value) {
            kotprefPreference.putStringSet("clocks", value)
        }
    var activeClockData by stringPrefVar("")
    var supportSeconds by booleanPrefVar(true)

    var activeClock: Clock?
        get() {
            val data = activeClockData
            if (data == "") return null
            return Clock.parse(activeClockData)
        }
        set(value) {
            if (value == null) activeClockData = ""
            else activeClockData = value.toString()
        }
}