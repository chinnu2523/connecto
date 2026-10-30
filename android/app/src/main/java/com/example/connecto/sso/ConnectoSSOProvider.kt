package com.example.connecto.sso

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.connecto.network.ConnectoApiClient

/**
 * Ecosystem SSO ContentProvider allowing authorized local applications (like Our Store)
 * to perform seamless 1-tap authentication using the active Connecto account.
 */
class ConnectoSSOProvider : ContentProvider() {

    companion object {
        private const val TAG = "ConnectoSSOProvider"
        const val AUTHORITY = "com.connecto.app.sso"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/session")
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(
            "is_logged_in",
            "username",
            "display_name",
            "avatar_url",
            "is_admin",
            "role",
            "token"
        ))

        try {
            val ctx = context ?: return cursor
            ConnectoApiClient.initSessionFromPrefs(ctx)

            val prefs = ctx.getSharedPreferences("connecto_session_prefs", android.content.Context.MODE_PRIVATE)
            val username = (prefs.getString("username", null) ?: ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@")
            val displayName = prefs.getString("display_name", null) ?: ConnectoApiClient.currentUserDisplayName ?: username
            val avatarUrl = prefs.getString("avatar_url", null) ?: ConnectoApiClient.currentUserAvatar ?: ""
            val token = prefs.getString("token", null) ?: ConnectoApiClient.sessionToken ?: ""
            val isLoggedIn = username.isNotBlank() && token.isNotBlank()

            val isAdmin = (username.equals("chinnu14754x", ignoreCase = true) ||
                           username.equals("admin", ignoreCase = true) ||
                           ConnectoApiClient.currentUserIsAdmin)

            val role = if (isAdmin) "admin" else "member"

            Log.d(TAG, "SSO Query processed: user=$username, isLoggedIn=$isLoggedIn, role=$role")

            cursor.addRow(arrayOf(
                if (isLoggedIn) 1 else 0,
                username,
                displayName,
                avatarUrl,
                if (isAdmin) 1 else 0,
                role,
                token
            ))
        } catch (e: Exception) {
            Log.e(TAG, "SSO Query failed: ${e.message}", e)
        }

        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.connecto.session"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
