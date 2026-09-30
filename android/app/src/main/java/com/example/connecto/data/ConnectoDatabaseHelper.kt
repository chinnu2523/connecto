package com.example.connecto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.connecto.network.ChannelDto
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.MessageDto
import com.example.connecto.network.PollDto
import com.example.connecto.network.PollOptionDto
import com.example.connecto.ui.screens.FriendItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ConnectoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "connecto_local_cache.db"
        private const val DATABASE_VERSION = 5

        @Volatile
        private var INSTANCE: ConnectoDatabaseHelper? = null

        fun getInstance(context: Context): ConnectoDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConnectoDatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val TABLE_MESSAGES = "cached_messages"
        private const val COL_MSG_ID = "id"
        private const val COL_MSG_CHANNEL = "channel_id"
        private const val COL_MSG_AUTHOR_ID = "author_id"
        private const val COL_MSG_AUTHOR_NAME = "author_name"
        private const val COL_MSG_CONTENT = "content"
        private const val COL_MSG_CREATED_AT = "created_at"
        private const val COL_MSG_AUTHOR_AVATAR = "author_avatar"
        private const val COL_MSG_TYPE = "msg_type"
        private const val COL_MSG_POLL_JSON = "poll_json"
        private const val COL_MSG_TIMER_SEC = "timer_seconds"
        private const val COL_MSG_EXPIRES_AT = "expires_at"

        private const val TABLE_CHANNELS = "cached_channels"
        private const val COL_CH_ID = "id"
        private const val COL_CH_NAME = "name"
        private const val COL_CH_TYPE = "type"

        private const val TABLE_FRIENDS = "cached_friends"
        private const val COL_FRIEND_ID = "id"
        private const val COL_FRIEND_NAME = "name"
        private const val COL_FRIEND_HANDLE = "handle"
        private const val COL_FRIEND_INITIAL = "initial"
        private const val COL_FRIEND_STATUS = "status"
        private const val COL_FRIEND_BIO = "bio"
        private const val COL_FRIEND_LAST_MSG = "last_message"
        private const val COL_FRIEND_TIME = "time_ago"
        private const val COL_FRIEND_UNREAD = "unread_count"
        private const val COL_FRIEND_IS_ONLINE = "is_online"
        private const val COL_FRIEND_AVATAR = "avatar_url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_MESSAGES (
                $COL_MSG_ID TEXT PRIMARY KEY,
                $COL_MSG_CHANNEL TEXT NOT NULL,
                $COL_MSG_AUTHOR_ID TEXT,
                $COL_MSG_AUTHOR_NAME TEXT,
                $COL_MSG_CONTENT TEXT,
                $COL_MSG_CREATED_AT TEXT,
                $COL_MSG_AUTHOR_AVATAR TEXT,
                $COL_MSG_TYPE TEXT DEFAULT 'text',
                $COL_MSG_POLL_JSON TEXT,
                $COL_MSG_TIMER_SEC INTEGER DEFAULT 0,
                $COL_MSG_EXPIRES_AT TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_msg_channel ON $TABLE_MESSAGES ($COL_MSG_CHANNEL)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_CHANNELS (
                $COL_CH_ID TEXT PRIMARY KEY,
                $COL_CH_NAME TEXT NOT NULL,
                $COL_CH_TYPE TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_FRIENDS (
                $COL_FRIEND_ID TEXT PRIMARY KEY,
                $COL_FRIEND_NAME TEXT NOT NULL,
                $COL_FRIEND_HANDLE TEXT NOT NULL,
                $COL_FRIEND_INITIAL TEXT NOT NULL,
                $COL_FRIEND_STATUS TEXT,
                $COL_FRIEND_BIO TEXT,
                $COL_FRIEND_LAST_MSG TEXT,
                $COL_FRIEND_TIME TEXT,
                $COL_FRIEND_UNREAD INTEGER DEFAULT 0,
                $COL_FRIEND_IS_ONLINE INTEGER DEFAULT 0,
                $COL_FRIEND_AVATAR TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_FRIENDS (
                    $COL_FRIEND_ID TEXT PRIMARY KEY,
                    $COL_FRIEND_NAME TEXT NOT NULL,
                    $COL_FRIEND_HANDLE TEXT NOT NULL,
                    $COL_FRIEND_INITIAL TEXT NOT NULL,
                    $COL_FRIEND_STATUS TEXT,
                    $COL_FRIEND_BIO TEXT,
                    $COL_FRIEND_LAST_MSG TEXT,
                    $COL_FRIEND_TIME TEXT,
                    $COL_FRIEND_UNREAD INTEGER DEFAULT 0,
                    $COL_FRIEND_IS_ONLINE INTEGER DEFAULT 0,
                    $COL_FRIEND_AVATAR TEXT
                )
                """.trimIndent()
            )
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABLE_FRIENDS ADD COLUMN $COL_FRIEND_AVATAR TEXT")
            } catch (_: Exception) {}
            try {
                db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_AUTHOR_AVATAR TEXT")
            } catch (_: Exception) {}
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("DELETE FROM $TABLE_MESSAGES")
                db.execSQL("DELETE FROM $TABLE_CHANNELS")
            } catch (_: Exception) {}
        }
        if (oldVersion < 5) {
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_TYPE TEXT DEFAULT 'text'") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_POLL_JSON TEXT") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_TIMER_SEC INTEGER DEFAULT 0") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_EXPIRES_AT TEXT") } catch (_: Exception) {}
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // NOTE: No DDL (ALTER TABLE) or heavy I/O here — onOpen is called on the Main thread
        // during first writableDatabase access. DDL lives exclusively in onUpgrade().
        // Purge is scheduled on a background IO thread via schedulePurgeAsync().
    }

    /** Schedule a background purge of expired/old messages. Safe to call from any thread. */
    @OptIn(DelicateCoroutinesApi::class)
    fun schedulePurgeAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            try { purgeMessagesOlderThan24Hours() } catch (_: Exception) {}
        }
    }

    private fun pollDtoToJson(poll: PollDto?): String? {
        if (poll == null) return null
        return try {
            val obj = JSONObject()
            obj.put("id", poll.id)
            obj.put("question", poll.question)
            obj.put("author", poll.author)
            obj.put("author_name", poll.authorName)
            obj.put("channel_id", poll.channelId)
            obj.put("multiple", poll.multiple)
            obj.put("total_votes", poll.totalVotes)
            obj.put("closed", poll.closed)
            obj.put("created_at", poll.createdAt)
            obj.put("timestamp", poll.timestamp)
            val arr = JSONArray()
            for (opt in poll.options) {
                val optObj = JSONObject()
                optObj.put("text", opt.text)
                val vArr = JSONArray()
                for (v in opt.votes) {
                    vArr.put(v)
                }
                optObj.put("votes", vArr)
                arr.put(optObj)
            }
            obj.put("options", arr)
            obj.toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun parseIsoEpochMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        val clean = dateStr.trim().replace("Z", "+0000").replace("T", " ")
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss.SSSSSSZ",
            "yyyy-MM-dd HH:mm:ss.SSSZ",
            "yyyy-MM-dd HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (fmt in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(clean)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return null
    }

    fun isOlderThan24Hours(createdAt: String?): Boolean {
        if (createdAt.isNullOrBlank()) return false
        val epoch = parseIsoEpochMillis(createdAt) ?: return false
        val diff = System.currentTimeMillis() - epoch
        return diff >= 24 * 60 * 60 * 1000L
    }

    fun purgeMessagesOlderThan24Hours(): Int {
        var count = 0
        try {
            val db = writableDatabase
            val cursor = db.query(TABLE_MESSAGES, arrayOf(COL_MSG_ID, COL_MSG_CREATED_AT, COL_MSG_EXPIRES_AT), null, null, null, null, null)
            val idsToDelete = mutableListOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val expiresAt = it.getString(2)
                    // Connecto maintains permanent chat history. ONLY purge ephemeral self-destruct messages whose timer has expired.
                    if (!expiresAt.isNullOrBlank() && isExpired(expiresAt)) {
                        idsToDelete.add(id)
                    }
                }
            }
            for (id in idsToDelete) {
                db.delete(TABLE_MESSAGES, "$COL_MSG_ID = ?", arrayOf(id))
            }
            count = idsToDelete.size
        } catch (_: Exception) {}
        return count
    }

    private fun isExpired(expiresAt: String?): Boolean {
        if (expiresAt.isNullOrBlank()) return false
        val epoch = parseIsoEpochMillis(expiresAt) ?: return false
        return System.currentTimeMillis() > epoch
    }

    fun saveMessage(msg: MessageDto) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_MSG_ID, msg.id)
            put(COL_MSG_CHANNEL, (msg.channelId ?: "general").trim().lowercase().removePrefix("#"))
            put(COL_MSG_AUTHOR_ID, msg.authorId)
            put(COL_MSG_AUTHOR_NAME, msg.authorName)
            put(COL_MSG_CONTENT, msg.content)
            put(COL_MSG_CREATED_AT, msg.createdAt)
            put(COL_MSG_AUTHOR_AVATAR, msg.authorAvatar)
            put(COL_MSG_TYPE, msg.type)
            put(COL_MSG_POLL_JSON, pollDtoToJson(msg.poll))
            put(COL_MSG_TIMER_SEC, msg.timerSeconds ?: 0)
            put(COL_MSG_EXPIRES_AT, msg.expiresAt)
        }
        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveMessages(channelId: String, messages: List<MessageDto>) {
        val db = writableDatabase
        val cleanCh = channelId.trim().lowercase().removePrefix("#")
        db.beginTransaction()
        try {
            for (msg in messages) {
                val cv = ContentValues().apply {
                    put(COL_MSG_ID, msg.id)
                    put(COL_MSG_CHANNEL, cleanCh)
                    put(COL_MSG_AUTHOR_ID, msg.authorId)
                    put(COL_MSG_AUTHOR_NAME, msg.authorName)
                    put(COL_MSG_CONTENT, msg.content)
                    put(COL_MSG_CREATED_AT, msg.createdAt)
                    put(COL_MSG_AUTHOR_AVATAR, msg.authorAvatar)
                    put(COL_MSG_TYPE, msg.type)
                    put(COL_MSG_POLL_JSON, pollDtoToJson(msg.poll))
                    put(COL_MSG_TIMER_SEC, msg.timerSeconds ?: 0)
                    put(COL_MSG_EXPIRES_AT, msg.expiresAt)
                }
                db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteMessage(messageId: String) {
        try {
            val db = writableDatabase
            db.delete(TABLE_MESSAGES, "$COL_MSG_ID = ?", arrayOf(messageId))
        } catch (_: Exception) {}
    }

    fun getMessagesForChannel(channelId: String, limit: Int = 50, beforeCreatedAt: String? = null): List<MessageDto> {
        val cleanCh = channelId.trim().lowercase().removePrefix("#")
        val db = readableDatabase
        val list = mutableListOf<MessageDto>()
        val selection = if (beforeCreatedAt.isNullOrBlank()) {
            "$COL_MSG_CHANNEL = ?"
        } else {
            "$COL_MSG_CHANNEL = ? AND $COL_MSG_CREATED_AT < ?"
        }
        val selectionArgs = if (beforeCreatedAt.isNullOrBlank()) {
            arrayOf(cleanCh)
        } else {
            arrayOf(cleanCh, beforeCreatedAt)
        }
        val cursor = db.query(
            TABLE_MESSAGES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_MSG_CREATED_AT DESC",
            limit.toString()
        )
        val expiredIds = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val avatarCol = it.getColumnIndex(COL_MSG_AUTHOR_AVATAR)
                val authorAvatar = if (avatarCol >= 0) it.getString(avatarCol) else null
                
                val typeCol = it.getColumnIndex(COL_MSG_TYPE)
                val msgType = if (typeCol >= 0) it.getString(typeCol) ?: "text" else "text"

                val pollCol = it.getColumnIndex(COL_MSG_POLL_JSON)
                val pollJson = if (pollCol >= 0) it.getString(pollCol) else null
                val poll = if (!pollJson.isNullOrBlank()) {
                    try {
                        ConnectoApiClient.parsePollDto(JSONObject(pollJson))
                    } catch (_: Exception) { null }
                } else null

                val timerCol = it.getColumnIndex(COL_MSG_TIMER_SEC)
                val timerSec = if (timerCol >= 0) it.getInt(timerCol) else 0

                val expCol = it.getColumnIndex(COL_MSG_EXPIRES_AT)
                val expiresAt = if (expCol >= 0) it.getString(expCol) else null

                val msgId = it.getString(it.getColumnIndexOrThrow(COL_MSG_ID))
                val createdAt = it.getString(it.getColumnIndexOrThrow(COL_MSG_CREATED_AT)) ?: "Just now"

                if (isExpired(expiresAt)) {
                    expiredIds.add(msgId)
                    continue
                }

                list.add(
                    MessageDto(
                        id = msgId,
                        channelId = it.getString(it.getColumnIndexOrThrow(COL_MSG_CHANNEL)),
                        authorId = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_ID)) ?: "gamer",
                        authorName = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_NAME)) ?: "Gamer",
                        content = it.getString(it.getColumnIndexOrThrow(COL_MSG_CONTENT)) ?: "",
                        createdAt = createdAt,
                        authorAvatar = authorAvatar,
                        type = msgType,
                        pollId = poll?.id,
                        poll = poll,
                        timerSeconds = if (timerSec > 0) timerSec else null,
                        expiresAt = expiresAt
                    )
                )
            }
        }
        if (expiredIds.isNotEmpty()) {
            try {
                val writeDb = writableDatabase
                for (id in expiredIds) {
                    writeDb.delete(TABLE_MESSAGES, "$COL_MSG_ID = ?", arrayOf(id))
                }
            } catch (_: Exception) {}
        }
        return list.reversed().distinctBy { it.id }
    }

    /**
     * Retrieves direct messages matching any known alias for the conversation:
     * canonical name (dm-min-max), alt canonical name (dm_min_max), reverse name, or channel UUID.
     * Guarantees 0ms local SQLite load regardless of which identifier was used to store messages.
     */
    fun getDirectMessages(
        myUsername: String,
        friendUsername: String,
        channelId: String? = null,
        limit: Int = 50,
        beforeCreatedAt: String? = null
    ): List<MessageDto> {
        val u1 = myUsername.trim().lowercase().removePrefix("@")
        val u2 = friendUsername.trim().lowercase().removePrefix("@")
        val canon1 = "dm-${minOf(u1, u2)}-${maxOf(u1, u2)}"
        val canon2 = "dm_${minOf(u1, u2)}_${maxOf(u1, u2)}"
        val rev1 = "dm-${maxOf(u1, u2)}-${minOf(u1, u2)}"
        val rev2 = "dm_${maxOf(u1, u2)}_${minOf(u1, u2)}"

        val candidates = mutableListOf(canon1, canon2, rev1, rev2)
        if (!channelId.isNullOrBlank()) {
            candidates.add(channelId.trim().lowercase().removePrefix("#"))
        }
        val distinctCandidates = candidates.distinct()

        val db = readableDatabase
        val list = mutableListOf<MessageDto>()
        val placeholders = distinctCandidates.joinToString(",") { "?" }
        val selection = if (beforeCreatedAt.isNullOrBlank()) {
            "$COL_MSG_CHANNEL IN ($placeholders)"
        } else {
            "$COL_MSG_CHANNEL IN ($placeholders) AND $COL_MSG_CREATED_AT < ?"
        }
        val selectionArgs = if (beforeCreatedAt.isNullOrBlank()) {
            distinctCandidates.toTypedArray()
        } else {
            (distinctCandidates + beforeCreatedAt).toTypedArray()
        }

        val cursor = db.query(
            TABLE_MESSAGES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_MSG_CREATED_AT DESC",
            limit.toString()
        )
        val expiredIds = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val avatarCol = it.getColumnIndex(COL_MSG_AUTHOR_AVATAR)
                val authorAvatar = if (avatarCol >= 0) it.getString(avatarCol) else null

                val typeCol = it.getColumnIndex(COL_MSG_TYPE)
                val msgType = if (typeCol >= 0) it.getString(typeCol) ?: "text" else "text"

                val pollCol = it.getColumnIndex(COL_MSG_POLL_JSON)
                val pollJson = if (pollCol >= 0) it.getString(pollCol) else null
                val poll = if (!pollJson.isNullOrBlank()) {
                    try {
                        ConnectoApiClient.parsePollDto(JSONObject(pollJson))
                    } catch (_: Exception) { null }
                } else null

                val timerCol = it.getColumnIndex(COL_MSG_TIMER_SEC)
                val timerSec = if (timerCol >= 0) it.getInt(timerCol) else 0

                val expCol = it.getColumnIndex(COL_MSG_EXPIRES_AT)
                val expiresAt = if (expCol >= 0) it.getString(expCol) else null

                val msgId = it.getString(it.getColumnIndexOrThrow(COL_MSG_ID))
                val createdAt = it.getString(it.getColumnIndexOrThrow(COL_MSG_CREATED_AT)) ?: "Just now"

                if (isExpired(expiresAt)) {
                    expiredIds.add(msgId)
                    continue
                }

                list.add(
                    MessageDto(
                        id = msgId,
                        channelId = it.getString(it.getColumnIndexOrThrow(COL_MSG_CHANNEL)),
                        authorId = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_ID)) ?: "gamer",
                        authorName = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_NAME)) ?: "Gamer",
                        content = it.getString(it.getColumnIndexOrThrow(COL_MSG_CONTENT)) ?: "",
                        createdAt = createdAt,
                        authorAvatar = authorAvatar,
                        type = msgType,
                        pollId = poll?.id,
                        poll = poll,
                        timerSeconds = if (timerSec > 0) timerSec else null,
                        expiresAt = expiresAt
                    )
                )
            }
        }
        if (expiredIds.isNotEmpty()) {
            try {
                val writeDb = writableDatabase
                for (id in expiredIds) {
                    writeDb.delete(TABLE_MESSAGES, "$COL_MSG_ID = ?", arrayOf(id))
                }
            } catch (_: Exception) {}
        }
        return list.reversed().distinctBy { it.id }
    }

    fun saveChannels(channels: List<ChannelDto>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (ch in channels) {
                val cv = ContentValues().apply {
                    put(COL_CH_ID, ch.id)
                    put(COL_CH_NAME, ch.name)
                    put(COL_CH_TYPE, ch.type)
                }
                db.insertWithOnConflict(TABLE_CHANNELS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getChannels(): List<ChannelDto> {
        val db = readableDatabase
        val list = mutableListOf<ChannelDto>()
        val cursor = db.query(TABLE_CHANNELS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ChannelDto(
                        id = it.getString(it.getColumnIndexOrThrow(COL_CH_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_CH_NAME)),
                        type = it.getString(it.getColumnIndexOrThrow(COL_CH_TYPE))
                    )
                )
            }
        }
        return list
    }

    fun saveFriends(friends: List<FriendItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_FRIENDS, null, null)
            val uniqueFriends = friends.distinctBy { it.handle.trim().lowercase().removePrefix("@") }
            for (f in uniqueFriends) {
                val cv = ContentValues().apply {
                    put(COL_FRIEND_ID, f.id)
                    put(COL_FRIEND_NAME, f.name)
                    put(COL_FRIEND_HANDLE, f.handle)
                    put(COL_FRIEND_INITIAL, f.initial)
                    put(COL_FRIEND_STATUS, f.status)
                    put(COL_FRIEND_BIO, f.bio)
                    put(COL_FRIEND_LAST_MSG, f.lastMessage)
                    put(COL_FRIEND_TIME, f.timeAgo)
                    put(COL_FRIEND_UNREAD, f.unreadCount)
                    put(COL_FRIEND_IS_ONLINE, if (f.isOnline) 1 else 0)
                    put(COL_FRIEND_AVATAR, f.avatarUrl)
                }
                db.insertWithOnConflict(TABLE_FRIENDS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedFriends(): List<FriendItem> {
        val db = readableDatabase
        val list = mutableListOf<FriendItem>()
        val cursor = db.query(TABLE_FRIENDS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val avatarCol = it.getColumnIndex(COL_FRIEND_AVATAR)
                val avatarUrl = if (avatarCol >= 0) it.getString(avatarCol) else null
                list.add(
                    FriendItem(
                        id = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_NAME)),
                        handle = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_HANDLE)),
                        initial = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_INITIAL)),
                        status = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_STATUS)) ?: "Accepted Friend",
                        bio = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_BIO)) ?: "Connecto Gamer",
                        lastMessage = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_LAST_MSG)) ?: "Direct chat active",
                        timeAgo = it.getString(it.getColumnIndexOrThrow(COL_FRIEND_TIME)) ?: "Active",
                        unreadCount = it.getInt(it.getColumnIndexOrThrow(COL_FRIEND_UNREAD)),
                        isOnline = it.getInt(it.getColumnIndexOrThrow(COL_FRIEND_IS_ONLINE)) == 1,
                        avatarUrl = avatarUrl
                    )
                )
            }
        }
        return list.distinctBy { it.handle.trim().lowercase().removePrefix("@") }
    }

    fun updateFriendOnlineStatus(identifier: String, isOnline: Boolean) {
        val db = writableDatabase
        val clean = identifier.trim().lowercase().removePrefix("@")
        val cv = ContentValues().apply {
            put(COL_FRIEND_IS_ONLINE, if (isOnline) 1 else 0)
            put(COL_FRIEND_STATUS, if (isOnline) "Online" else "Offline")
        }
        db.update(
            TABLE_FRIENDS,
            cv,
            "LOWER($COL_FRIEND_ID) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_NAME) = ?",
            arrayOf(clean, clean, "@$clean", clean)
        )
    }

    fun deleteFriend(friendIdOrHandle: String) {
        val db = writableDatabase
        val clean = friendIdOrHandle.trim().removePrefix("@")
        db.delete(
            TABLE_FRIENDS,
            "$COL_FRIEND_ID = ? OR $COL_FRIEND_HANDLE = ? OR $COL_FRIEND_HANDLE = ?",
            arrayOf(friendIdOrHandle, clean, "@$clean")
        )
    }

    fun clearAllCache() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_MESSAGES, null, null)
            db.delete(TABLE_CHANNELS, null, null)
            db.delete(TABLE_FRIENDS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
