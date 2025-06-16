import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

// Platform expectation remains the same
actual object PlatformSettingsProvider {
    actual fun provideSettings(): Settings {
        return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}
