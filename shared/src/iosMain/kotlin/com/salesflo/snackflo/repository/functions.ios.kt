package com.salesflo.snackflo.repository

import platform.Foundation.NSUserDefaults


class IOSSettingsDataStore : SettingsDataStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override suspend fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }
}


