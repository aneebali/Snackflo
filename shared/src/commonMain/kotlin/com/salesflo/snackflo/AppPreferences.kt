// In commonMain/src/AppSettings.kt
import com.russhwolf.settings.Settings

object AppPreferences {
    // ======================
    // Key Constants
    // ======================
    private object Keys {
        // Auth
        const val IS_LOGGED_IN = "is_logged_in"
        const val IS_FIRST_LOGIN = "is_first_login"
        const val IS_BIOMETRIC_ENABLED = "is_biometric_enabled"

        // Tokens

        const val REFRESH_TOKEN = "refresh_token"

        // User
        const val USER_TYPE = "user_type"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"

        // App Config
        const val IS_DARK_MODE = "is_dark_mode"
        const val APP_LANGUAGE = "app_language"
        const val APP_VERSION = "app_version"

        // Features
        const val FEATURE_X = "feature_x"
        const val FEATURE_Y = "feature_y"
    }

    // ======================
    // Settings Instance
    // ======================
    private val settings: Settings by lazy {
        PlatformSettingsProvider.provideSettings()
    }

    // ======================
    // Boolean Flag Functions
    // ======================
    fun setBooleanFlag(key: String, value: Boolean) = settings.putBoolean(key, value)
    fun getBooleanFlag(key: String, default: Boolean = false): Boolean =
        settings.getBoolean(key, default)

    fun containsFlag(key: String): Boolean = settings.hasKey(key)
    fun removeFlag(key: String) = settings.remove(key)

    // ======================
    // String Value Functions
    // ======================
    fun setStringValue(key: String, value: String) = settings.putString(key, value)
    fun getStringValue(key: String, default: String = ""): String = settings.getString(key, default)

    // ======================
    // Type-Safe Wrappers
    // ======================

    // Authentication
    var isLoggedIn: Boolean
        get() = getBooleanFlag(Keys.IS_LOGGED_IN)
        set(value) = setBooleanFlag(Keys.IS_LOGGED_IN, value)


    var userType: String
        get() = getStringValue(Keys.USER_TYPE)
        set(value) = setStringValue(Keys.USER_TYPE, value)

    var userId: String
        get() = getStringValue(Keys.USER_ID)
        set(value) = setStringValue(Keys.USER_ID, value)

    var userName: String
        get() = getStringValue(Keys.USER_NAME)
        set(value) = setStringValue(Keys.USER_NAME, value)


    // App Configuration
    var isDarkMode: Boolean
        get() = getBooleanFlag(Keys.IS_DARK_MODE)
        set(value) = setBooleanFlag(Keys.IS_DARK_MODE, value)

    // Feature Flags
    var isFeatureXEnabled: Boolean
        get() = getBooleanFlag(Keys.FEATURE_X)
        set(value) = setBooleanFlag(Keys.FEATURE_X, value)

    // ======================
    // Session Management
    // ======================
    fun clearSession() {
        setBooleanFlag(Keys.IS_LOGGED_IN, false)
        setStringValue(Keys.REFRESH_TOKEN, "")
        setStringValue(Keys.USER_ID, "")
    }

    fun clearAll() {
        settings.clear()
    }

    fun isFirstAppRun(): Boolean {
        if (getBooleanFlag(Keys.IS_FIRST_LOGIN, true)) {
            setBooleanFlag(Keys.IS_FIRST_LOGIN, false)
            return true
        }
        return false
    }
}

// Platform expectation remains the same
expect object PlatformSettingsProvider {
    fun provideSettings(): Settings
}