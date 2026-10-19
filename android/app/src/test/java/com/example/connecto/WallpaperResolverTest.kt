package com.example.connecto

import android.content.SharedPreferences
import com.example.connecto.data.WallpaperResolver
import com.example.connecto.ui.screens.FriendItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WallpaperResolverTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /**
     * In-memory fake SharedPreferences for JVM unit testing without Android runtime.
     */
    private class FakeSharedPreferences : SharedPreferences {
        val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map.toMutableMap()
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            (map[key] as? Set<*>)?.map { it.toString() }?.toMutableSet() ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): SharedPreferences.Editor = FakeEditor(this)

        private class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removes = mutableSetOf<String>()

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) pending[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) {
                    removes.add(key)
                    pending.remove(key)
                }
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                prefs.map.clear()
                pending.clear()
                removes.clear()
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                for (r in removes) prefs.map.remove(r)
                for ((k, v) in pending) prefs.map[k] = v
                pending.clear()
                removes.clear()
            }
        }
    }

    private fun createFriend(id: String = "usr_srinu_42", handle: String = "@srinu", name: String = "Srinu"): FriendItem {
        return FriendItem(
            id = id,
            name = name,
            handle = handle,
            initial = "S",
            status = "Hey there!",
            bio = "Bio",
            lastMessage = "Hello"
        )
    }

    @Test
    fun testRootCause_LegacyUnscopedPresetWallpaper_ResolvesAndAutoHeals() {
        val prefs = FakeSharedPreferences()
        // Simulate pre-update state: Wallpaper was saved under unscoped legacy key
        prefs.edit().putString("chat_wallpaper_usr_srinu_42", "glass_mesh").apply()

        val friend = createFriend()
        val (wpId, uri) = WallpaperResolver.resolve(
            friend = friend,
            myUsername = "admin",
            prefs = prefs,
            filesDir = null
        )

        // Verifies the bug is fixed: Wallpaper resolves to "glass_mesh" instead of reverting to "default"
        assertEquals("glass_mesh", wpId)
        assertNull(uri)

        // Verifies auto-heal: Migrated forward to scoped canonical keys
        assertEquals("glass_mesh", prefs.getString("admin_chat_wallpaper_dm-admin-srinu", null))
        assertEquals("glass_mesh", prefs.getString("admin_chat_wallpaper_usr_srinu_42", null))
        assertEquals("glass_mesh", prefs.getString("admin_chat_wallpaper_srinu", null))
    }

    @Test
    fun testRootCause_LegacyCustomWallpaper_ResolvesWithValidFileUriAndAutoHeals() {
        val prefs = FakeSharedPreferences()
        val customImagePath = "/data/user/0/com.example.connecto/files/chat_wallpapers/wp_srinu.jpg"
        // Simulate pre-update state: Custom wallpaper stored under legacy unscoped key with raw path
        prefs.edit()
            .putString("chat_wallpaper_custom_srinu", customImagePath)
            .apply()

        val friend = createFriend()
        val (wpId, uri) = WallpaperResolver.resolve(
            friend = friend,
            myUsername = "admin",
            prefs = prefs,
            filesDir = null
        )

        assertEquals("custom", wpId)
        // Sanitized to valid file URI
        assertEquals("file://$customImagePath", uri)

        // Verifies auto-heal saved the scoped canonical key
        assertEquals("custom", prefs.getString("admin_chat_wallpaper_dm-admin-srinu", null))
        assertEquals("file://$customImagePath", prefs.getString("admin_chat_wallpaper_custom_dm-admin-srinu", null))
    }

    @Test
    fun testRootCause_OnDiskImageFallback_WhenPreferencesWereLostOrReset() {
        val prefs = FakeSharedPreferences()
        val mockFilesDir = tempFolder.newFolder("files")
        val wpDir = File(mockFilesDir, "chat_wallpapers").apply { mkdirs() }
        val imageFile = File(wpDir, "wp_srinu.jpg").apply {
            writeBytes(ByteArray(1024) { 0xFF.toByte() })
        }

        val friend = createFriend()
        val (wpId, uri) = WallpaperResolver.resolve(
            friend = friend,
            myUsername = "admin",
            prefs = prefs,
            filesDir = mockFilesDir
        )

        // Discovered from disk fallback
        assertEquals("custom", wpId)
        assertEquals("file://${imageFile.absolutePath}", uri)

        // Auto-healed back into preferences
        assertEquals("custom", prefs.getString("admin_chat_wallpaper_dm-admin-srinu", null))
        assertEquals("file://${imageFile.absolutePath}", prefs.getString("admin_chat_wallpaper_custom_dm-admin-srinu", null))
    }

    @Test
    fun testRootCause_HandleVsIdMismatchResolution() {
        val prefs = FakeSharedPreferences()
        // Wallpaper was saved under cleanHandle "srinu"
        prefs.edit().putString("admin_chat_wallpaper_srinu", "sunset_glow").apply()

        // Friend loaded with id "usr_srinu_42"
        val friend = createFriend(id = "usr_srinu_42", handle = "@srinu")
        val (wpId, uri) = WallpaperResolver.resolve(
            friend = friend,
            myUsername = "admin",
            prefs = prefs,
            filesDir = null
        )

        assertEquals("sunset_glow", wpId)
        assertNull(uri)
    }

    @Test
    fun testModernScopedKeyHasPrecedenceOverLegacyKey() {
        val prefs = FakeSharedPreferences()
        // Legacy key had "glass_mesh"
        prefs.edit().putString("chat_wallpaper_srinu", "glass_mesh").apply()
        // Modern scoped key was explicitly changed to "dark_nebula"
        prefs.edit().putString("admin_chat_wallpaper_dm-admin-srinu", "dark_nebula").apply()

        val friend = createFriend()
        val (wpId, uri) = WallpaperResolver.resolve(
            friend = friend,
            myUsername = "admin",
            prefs = prefs,
            filesDir = null
        )

        // Modern key wins
        assertEquals("dark_nebula", wpId)
        assertNull(uri)
    }

    @Test
    fun testResolveTransform_Defaults() {
        val prefs = FakeSharedPreferences()
        val friend = createFriend()
        val transform = WallpaperResolver.resolveTransform(friend, "admin", prefs)

        assertEquals(1.0f, transform.scale, 0.001f)
        assertEquals(0f, transform.offsetX, 0.001f)
        assertEquals(0f, transform.offsetY, 0.001f)
        assertEquals("crop", transform.contentScaleMode)
        assertEquals(0.50f, transform.dim, 0.001f)
        assertEquals(0f, transform.blur, 0.001f)
    }

    @Test
    fun testSaveAndResolveTransform_CustomSizeAndPosition() {
        val prefs = FakeSharedPreferences()
        val friend = createFriend(id = "usr_srinu_42", handle = "@srinu")

        val customTransform = com.example.connecto.data.WallpaperTransform(
            scale = 1.6f,
            offsetX = 45f,
            offsetY = -30f,
            contentScaleMode = "fit",
            dim = 0.65f,
            blur = 8f
        )

        WallpaperResolver.saveTransform(friend, "admin", prefs, customTransform)

        // Resolve back
        val resolved = WallpaperResolver.resolveTransform(friend, "admin", prefs)

        assertEquals(1.6f, resolved.scale, 0.001f)
        assertEquals(45f, resolved.offsetX, 0.001f)
        assertEquals(-30f, resolved.offsetY, 0.001f)
        assertEquals("fit", resolved.contentScaleMode)
        assertEquals(0.65f, resolved.dim, 0.001f)
        assertEquals(8f, resolved.blur, 0.001f)

        // Verify persisted across candidate IDs
        assertEquals(1.6f, prefs.getFloat("admin_chat_wallpaper_scale_dm-admin-srinu", 1.0f), 0.001f)
        assertEquals(45f, prefs.getFloat("admin_chat_wallpaper_offset_x_dm-admin-srinu", 0f), 0.001f)
        assertEquals("fit", prefs.getString("admin_chat_wallpaper_mode_dm-admin-srinu", null))
    }

    @Test
    fun testResolveTransform_LegacyUnscopedFallback() {
        val prefs = FakeSharedPreferences()
        prefs.edit()
            .putFloat("chat_wallpaper_scale_srinu", 1.4f)
            .putFloat("chat_wallpaper_offset_y_srinu", 80f)
            .putString("chat_wallpaper_mode_srinu", "fill_bounds")
            .apply()

        val friend = createFriend(id = "usr_srinu_42", handle = "@srinu")
        val resolved = WallpaperResolver.resolveTransform(friend, "admin", prefs)

        assertEquals(1.4f, resolved.scale, 0.001f)
        assertEquals(80f, resolved.offsetY, 0.001f)
        assertEquals("fill_bounds", resolved.contentScaleMode)
    }
}
