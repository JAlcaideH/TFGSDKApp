package com.jahtfg.safecycle.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
object AppPreferences {
    private var sharedPreferences: SharedPreferences? = null

    fun setup(context: Context) {
        sharedPreferences = context.getSharedPreferences("SafeCycle.sharedprefs", MODE_PRIVATE)
    }

    var apiKeyMapBox: String?
        get() = Key.APIKEYMAP.getString()
        set(value) = Key.APIKEYMAP.setString(value)

    private enum class Key {
            APIKEYMAP;

        fun getString(): String? = if (sharedPreferences!!.contains(name)) sharedPreferences!!.getString(name, "") else null

        fun setString(value: String?) {
            sharedPreferences?.edit()?.apply {
                putString(name, value)
                apply()
            }
        }

        fun exists(): Boolean = sharedPreferences!!.contains(name)
    }
}