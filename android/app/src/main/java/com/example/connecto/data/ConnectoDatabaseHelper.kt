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
        private const val DATABASE_VERSION = 9

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
        private const val COL_MSG_REPLY_TO_ID = "reply_to_id"
        private const val COL_MSG_REPLY_TO_CONTENT = "reply_to_content"
        private const val COL_MSG_REPLY_TO_AUTHOR = "reply_to_author"

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
        private const val COL_FRIEND_BANNER = "banner_url"
        private const val COL_FRIEND_LAST_TIMESTAMP = "last_message_timestamp"

        private const val TABLE_GROUPS = "cached_groups"
        private const val COL_GROUP_ID = "id"
        private const val COL_GROUP_NAME = "name"
        private const val COL_GROUP_HANDLE = "handle"
        private const val COL_GROUP_INITIAL = "initial"
        private const val COL_GROUP_STATUS = "status"
        private const val COL_GROUP_BIO = "bio"
        private const val COL_GROUP_LAST_MSG = "last_message"
        private const val COL_GROUP_TIME = "time_ago"
        private const val COL_GROUP_UNREAD = "unread_count"
        private const val COL_GROUP_AVATAR = "avatar_url"
        private const val COL_GROUP_IS_CREATED = "is_created_by_me"
        private const val COL_GROUP_MEMBER_COUNT = "member_count"
        private const val COL_GROUP_MEMBERS_JSON = "members_json"
        private const val COL_GROUP_ADMIN = "admin"
        private const val COL_GROUP_CREATOR = "creator"
        private const val COL_GROUP_LAST_TIMESTAMP = "last_message_timestamp"
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
                $COL_MSG_EXPIRES_AT TEXT,
                $COL_MSG_REPLY_TO_ID TEXT,
                $COL_MSG_REPLY_TO_CONTENT TEXT,
                $COL_MSG_REPLY_TO_AUTHOR TEXT
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
                $COL_FRIEND_AVATAR TEXT,
                $COL_FRIEND_BANNER TEXT,
                $COL_FRIEND_LAST_TIMESTAMP INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_GROUPS (
                $COL_GROUP_ID TEXT PRIMARY KEY,
                $COL_GROUP_NAME TEXT NOT NULL,
                $COL_GROUP_HANDLE TEXT NOT NULL,
                $COL_GROUP_INITIAL TEXT NOT NULL,
                $COL_GROUP_STATUS TEXT,
                $COL_GROUP_BIO TEXT,
                $COL_GROUP_LAST_MSG TEXT,
                $COL_GROUP_TIME TEXT,
                $COL_GROUP_UNREAD INTEGER DEFAULT 0,
                $COL_GROUP_AVATAR TEXT,
                $COL_GROUP_IS_CREATED INTEGER DEFAULT 0,
                $COL_GROUP_MEMBER_COUNT INTEGER DEFAULT 0,
                $COL_GROUP_MEMBERS_JSON TEXT,
                $COL_GROUP_ADMIN TEXT,
                $COL_GROUP_CREATOR TEXT,
                $COL_GROUP_LAST_TIMESTAMP INTEGER DEFAULT 0
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
                    $COL_FRIEND_AVATAR TEXT,
                    $COL_FRIEND_BANNER TEXT,
                    $COL_FRIEND_LAST_TIMESTAMP INTEGER DEFAULT 0
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
        if (oldVersion < 6) {
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_REPLY_TO_ID TEXT") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_REPLY_TO_CONTENT TEXT") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COL_MSG_REPLY_TO_AUTHOR TEXT") } catch (_: Exception) {}
        }
        if (oldVersion < 7) {
            try { db.execSQL("ALTER TABLE $TABLE_FRIENDS ADD COLUMN $COL_FRIEND_BANNER TEXT") } catch (_: Exception) {}
        }
        if (oldVersion < 8) {
            try { db.execSQL("ALTER TABLE $TABLE_FRIENDS ADD COLUMN $COL_FRIEND_LAST_TIMESTAMP INTEGER DEFAULT 0") } catch (_: Exception) {}
        }
        if (oldVersion < 9) {
            try {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS $TABLE_GROUPS (
                        $COL_GROUP_ID TEXT PRIMARY KEY,
                        $COL_GROUP_NAME TEXT NOT NULL,
                        $COL_GROUP_HANDLE TEXT NOT NULL,
                        $COL_GROUP_INITIAL TEXT NOT NULL,
                        $COL_GROUP_STATUS TEXT,
                        $COL_GROUP_BIO TEXT,
                        $COL_GROUP_LAST_MSG TEXT,
                        $COL_GROUP_TIME TEXT,
                        $COL_GROUP_UNREAD INTEGER DEFAULT 0,
                        $COL_GROUP_AVATAR TEXT,
                        $COL_GROUP_IS_CREATED INTEGER DEFAULT 0,
                        $COL_GROUP_MEMBER_COUNT INTEGER DEFAULT 0,
                        $COL_GROUP_MEMBERS_JSON TEXT,
                        $COL_GROUP_ADMIN TEXT,
                        $COL_GROUP_CREATOR TEXT,
                        $COL_GROUP_LAST_TIMESTAMP INTEGER DEFAULT 0
                    )
                    """.trimIndent()
                )
            } catch (_: Exception) {}
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
            try { purgeOptimisticMessages() } catch (_: Exception) {}
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

    fun purgeOptimisticMessages(): Int {
        var count = 0
        try {
            val db = writableDatabase
            count = db.delete(TABLE_MESSAGES, "$COL_MSG_ID LIKE 'opt-%'", null)
        } catch (_: Exception) {}
        return count
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

    fun deduplicateMessages(messages: List<MessageDto>): List<MessageDto> {
        val result = mutableListOf<MessageDto>()
        for (msg in messages) {
            val existingIndex = result.indexOfFirst { existing ->
                if (existing.id == msg.id) return@indexOfFirst true
                val sameContent = existing.content.trim() == msg.content.trim() && msg.content.isNotBlank()
                if (!sameContent) return@indexOfFirst false

                val u1 = existing.authorId.trim().lowercase().removePrefix("@")
                val u2 = msg.authorId.trim().lowercase().removePrefix("@")
                val n1 = existing.authorName.trim().lowercase().removePrefix("@")
                val n2 = msg.authorName.trim().lowercase().removePrefix("@")
                val sameSender = u1 == u2 || n1 == n2 || u1 == n2 || u2 == n1

                if (!sameSender) return@indexOfFirst false

                val t1 = parseIsoEpochMillis(existing.createdAt) ?: 0L
                val t2 = parseIsoEpochMillis(msg.createdAt) ?: 0L
                if (t1 > 0L && t2 > 0L) {
                    kotlin.math.abs(t1 - t2) <= 30_000L
                } else {
                    existing.createdAt == msg.createdAt
                }
            }
            if (existingIndex == -1) {
                result.add(msg)
            } else {
                val existing = result[existingIndex]
                val existingIsTemp = existing.id.startsWith("opt-") || existing.id.startsWith("ws_")
                val newIsTemp = msg.id.startsWith("opt-") || msg.id.startsWith("ws_")
                if (existingIsTemp && !newIsTemp) {
                    result[existingIndex] = msg
                }
            }
        }
        return result
    }

    fun saveMessage(msg: MessageDto) {
        val db = writableDatabase
        // When saving a confirmed message, automatically purge temporary optimistic/ws messages with the same content
        if (!msg.id.startsWith("opt-") && !msg.id.startsWith("ws_") && msg.content.isNotBlank()) {
            try {
                db.delete(
                    TABLE_MESSAGES,
                    "($COL_MSG_ID LIKE 'opt-%' OR $COL_MSG_ID LIKE 'ws_%') AND $COL_MSG_CONTENT = ?",
                    arrayOf(msg.content)
                )
            } catch (_: Exception) {}
        }
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
            put(COL_MSG_REPLY_TO_ID, msg.replyToId)
            put(COL_MSG_REPLY_TO_CONTENT, msg.replyToContent)
            put(COL_MSG_REPLY_TO_AUTHOR, msg.replyToAuthor)
        }
        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveMessages(channelId: String, messages: List<MessageDto>) {
        val db = writableDatabase
        val cleanCh = channelId.trim().lowercase().removePrefix("#")
        db.beginTransaction()
        try {
            for (msg in messages) {
                if (!msg.id.startsWith("opt-") && !msg.id.startsWith("ws_") && msg.content.isNotBlank()) {
                    try {
                        db.delete(
                            TABLE_MESSAGES,
                            "($COL_MSG_ID LIKE 'opt-%' OR $COL_MSG_ID LIKE 'ws_%') AND $COL_MSG_CONTENT = ?",
                            arrayOf(msg.content)
                        )
                    } catch (_: Exception) {}
                }
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
                    put(COL_MSG_REPLY_TO_ID, msg.replyToId)
                    put(COL_MSG_REPLY_TO_CONTENT, msg.replyToContent)
                    put(COL_MSG_REPLY_TO_AUTHOR, msg.replyToAuthor)
                }
                db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
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

                val replyIdCol = it.getColumnIndex(COL_MSG_REPLY_TO_ID)
                val replyToId = if (replyIdCol >= 0) it.getString(replyIdCol) else null
                val replyContentCol = it.getColumnIndex(COL_MSG_REPLY_TO_CONTENT)
                val replyToContent = if (replyContentCol >= 0) it.getString(replyContentCol) else null
                val replyAuthorCol = it.getColumnIndex(COL_MSG_REPLY_TO_AUTHOR)
                val replyToAuthor = if (replyAuthorCol >= 0) it.getString(replyAuthorCol) else null

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
                        expiresAt = expiresAt,
                        replyToId = replyToId,
                        replyToContent = replyToContent,
                        replyToAuthor = replyToAuthor
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
        return deduplicateMessages(list.reversed())
    }

    /**
     * Retrieves direct messages matching any known alias for the conversation:
     * canonical name (dm-min-max), alt canonical name (dm_min_max), reverse name, channel UUID,
     * or any messages between the two users discovered in local SQLite cache.
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
        val u1Base = u1.replace(Regex("\\d+$"), "")
        val u2Base = u2.replace(Regex("\\d+$"), "")

        val canon1 = "dm-${minOf(u1, u2)}-${maxOf(u1, u2)}"
        val canon2 = "dm_${minOf(u1, u2)}_${maxOf(u1, u2)}"
        val rev1 = "dm-${maxOf(u1, u2)}-${minOf(u1, u2)}"
        val rev2 = "dm_${maxOf(u1, u2)}_${minOf(u1, u2)}"

        val candidates = mutableListOf(canon1, canon2, rev1, rev2, u2, "@$u2", "dm-$u2", "dm_$u2")

        if (u1Base.isNotEmpty() && u1Base != u1) {
            candidates.add("dm-${minOf(u1Base, u2)}-${maxOf(u1Base, u2)}")
            candidates.add("dm_${minOf(u1Base, u2)}_${maxOf(u1Base, u2)}")
            candidates.add("dm-${maxOf(u1Base, u2)}-${minOf(u1Base, u2)}")
            candidates.add("dm_${maxOf(u1Base, u2)}_${minOf(u1Base, u2)}")
        }
        if (u2Base.isNotEmpty() && u2Base != u2) {
            candidates.add("dm-${minOf(u1, u2Base)}-${maxOf(u1, u2Base)}")
            candidates.add("dm_${minOf(u1, u2Base)}_${maxOf(u1, u2Base)}")
            candidates.add("dm-${maxOf(u1, u2Base)}-${minOf(u1, u2Base)}")
            candidates.add("dm_${maxOf(u1, u2Base)}_${minOf(u1, u2Base)}")
            candidates.add(u2Base)
            candidates.add("@$u2Base")
        }

        if (!channelId.isNullOrBlank()) {
            val cleanCh = channelId.trim().lowercase().removePrefix("#")
            if (cleanCh.startsWith("dm-") || cleanCh.startsWith("dm_")) {
                val hasU1 = cleanCh.contains(u1) || (u1Base.isNotEmpty() && cleanCh.contains(u1Base))
                val hasU2 = cleanCh.contains(u2) || (u2Base.isNotEmpty() && cleanCh.contains(u2Base))
                if (hasU1 && hasU2) {
                    candidates.add(cleanCh)
                }
            } else {
                // If it's a UUID, verify it does not belong to another conversation in TABLE_CHANNELS
                var isAlien = false
                try {
                    val chCursor = readableDatabase.rawQuery(
                        "SELECT $COL_CH_NAME FROM $TABLE_CHANNELS WHERE $COL_CH_ID = ? LIMIT 1",
                        arrayOf(cleanCh)
                    )
                    chCursor.use { cur ->
                        if (cur.moveToFirst()) {
                            val chName = cur.getString(0)?.lowercase() ?: ""
                            if (chName.startsWith("dm-") || chName.startsWith("dm_")) {
                                val hasU1 = chName.contains(u1) || (u1Base.isNotEmpty() && chName.contains(u1Base))
                                val hasU2 = chName.contains(u2) || (u2Base.isNotEmpty() && chName.contains(u2Base))
                                if (!hasU1 || !hasU2) {
                                    isAlien = true
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
                if (!isAlien) {
                    candidates.add(cleanCh)
                }
            }
        }

        val db = readableDatabase

        // 1. Look up any channel mappings stored in TABLE_CHANNELS (e.g. type = 'dm')
        try {
            val chCursor = db.rawQuery(
                "SELECT $COL_CH_ID, $COL_CH_NAME FROM $TABLE_CHANNELS WHERE $COL_CH_TYPE = 'dm'",
                null
            )
            chCursor.use { cur ->
                while (cur.moveToNext()) {
                    val chId = cur.getString(0)?.trim()?.lowercase()?.removePrefix("#") ?: continue
                    val chName = cur.getString(1)?.trim()?.lowercase()?.removePrefix("#") ?: ""
                    val hasU1 = chName.contains(u1) || (u1Base.isNotEmpty() && chName.contains(u1Base))
                    val hasU2 = chName.contains(u2) || (u2Base.isNotEmpty() && chName.contains(u2Base))
                    if (candidates.contains(chName) || (hasU1 && hasU2)) {
                        candidates.add(chId)
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Discover all friend identifiers (id, name, handle) from TABLE_FRIENDS
        val friendIdentifiers = mutableSetOf(u2)
        if (u2Base.isNotEmpty()) friendIdentifiers.add(u2Base)
        try {
            val friendCursor = db.rawQuery(
                "SELECT $COL_FRIEND_ID, $COL_FRIEND_NAME, $COL_FRIEND_HANDLE FROM $TABLE_FRIENDS WHERE lower($COL_FRIEND_HANDLE) = ? OR lower($COL_FRIEND_HANDLE) = ? OR lower($COL_FRIEND_NAME) = ? OR $COL_FRIEND_ID = ?",
                arrayOf(u2, "@$u2", u2, u2)
            )
            friendCursor.use { cur ->
                while (cur.moveToNext()) {
                    val fId = cur.getString(0)?.trim()?.lowercase()
                    val fName = cur.getString(1)?.trim()?.lowercase()
                    val fHandle = cur.getString(2)?.trim()?.lowercase()?.removePrefix("@")
                    if (!fId.isNullOrBlank()) friendIdentifiers.add(fId)
                    if (!fName.isNullOrBlank()) friendIdentifiers.add(fName)
                    if (!fHandle.isNullOrBlank()) friendIdentifiers.add(fHandle)
                }
            }
        } catch (_: Exception) {}

        // 3. User identifiers for logged-in user
        val myIdentifiers = mutableSetOf(u1)
        if (u1Base.isNotEmpty()) myIdentifiers.add(u1Base)
        ConnectoApiClient.currentUserId?.trim()?.lowercase()?.let { if (it.isNotEmpty()) myIdentifiers.add(it) }
        ConnectoApiClient.currentUserDisplayName?.trim()?.lowercase()?.let { if (it.isNotEmpty()) myIdentifiers.add(it) }

        val allowedAuthors = (myIdentifiers + friendIdentifiers).map { it.lowercase() }.toSet()

        friendIdentifiers.forEach { fId ->
            if (fId.isNotBlank() && !candidates.contains(fId)) {
                candidates.add(fId)
            }
        }

        val distinctCandidates = candidates.distinct()

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
                val authorId = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_ID))?.trim()?.lowercase() ?: ""
                val authorName = it.getString(it.getColumnIndexOrThrow(COL_MSG_AUTHOR_NAME))?.trim()?.lowercase() ?: ""

                val isAuthorValid = allowedAuthors.isEmpty() ||
                    allowedAuthors.contains(authorId) ||
                    allowedAuthors.contains(authorName) ||
                    allowedAuthors.any { allowed -> (authorId.isNotEmpty() && authorId.contains(allowed)) || (authorName.isNotEmpty() && authorName.contains(allowed)) }

                if (!isAuthorValid) {
                    continue // Strictly skip messages from third parties/colliding accounts
                }

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

                val replyIdCol = it.getColumnIndex(COL_MSG_REPLY_TO_ID)
                val replyToId = if (replyIdCol >= 0) it.getString(replyIdCol) else null
                val replyContentCol = it.getColumnIndex(COL_MSG_REPLY_TO_CONTENT)
                val replyToContent = if (replyContentCol >= 0) it.getString(replyContentCol) else null
                val replyAuthorCol = it.getColumnIndex(COL_MSG_REPLY_TO_AUTHOR)
                val replyToAuthor = if (replyAuthorCol >= 0) it.getString(replyAuthorCol) else null

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
                        expiresAt = expiresAt,
                        replyToId = replyToId,
                        replyToContent = replyToContent,
                        replyToAuthor = replyToAuthor
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
        return deduplicateMessages(list.reversed())
    }

    fun deleteMessage(messageId: String) {
        try {
            val writeDb = writableDatabase
            writeDb.delete(TABLE_MESSAGES, "$COL_MSG_ID = ?", arrayOf(messageId))
        } catch (_: Exception) {}
    }

    fun updateMessageContent(messageId: String, newContent: String) {
        try {
            val writeDb = writableDatabase
            val cv = ContentValues().apply {
                put(COL_MSG_CONTENT, newContent)
            }
            writeDb.update(TABLE_MESSAGES, cv, "$COL_MSG_ID = ?", arrayOf(messageId))
        } catch (_: Exception) {}
    }

    fun saveDmChannel(canonicalName: String, channelId: String) {
        try {
            val db = writableDatabase
            val cv = ContentValues().apply {
                put(COL_CH_ID, channelId.trim().lowercase().removePrefix("#"))
                put(COL_CH_NAME, canonicalName.trim().lowercase().removePrefix("#"))
                put(COL_CH_TYPE, "dm")
            }
            db.insertWithOnConflict(TABLE_CHANNELS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (_: Exception) {}
    }

    fun getDmChannelId(canonicalName: String): String? {
        try {
            val db = readableDatabase
            val c = canonicalName.trim().lowercase().removePrefix("#")
            val cursor = db.rawQuery(
                "SELECT $COL_CH_ID FROM $TABLE_CHANNELS WHERE lower($COL_CH_NAME) = ? AND $COL_CH_TYPE = 'dm' LIMIT 1",
                arrayOf(c)
            )
            cursor.use {
                if (it.moveToFirst()) return it.getString(0)
            }
        } catch (_: Exception) {}
        return null
    }

    fun getAllDmChannels(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT $COL_CH_NAME, $COL_CH_ID FROM $TABLE_CHANNELS WHERE $COL_CH_TYPE = 'dm'",
                null
            )
            cursor.use {
                while (it.moveToNext()) {
                    val name = it.getString(0) ?: continue
                    val id = it.getString(1) ?: continue
                    map[name.lowercase()] = id
                    map[id.lowercase()] = name.lowercase()
                }
            }
        } catch (_: Exception) {}
        return map
    }

    fun saveChannels(channels: List<ChannelDto>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (ch in channels) {
                val cv = ContentValues().apply {
                    put(COL_CH_ID, ch.id)
                    put(COL_CH_NAME, ch.name)
                    val effectiveType = if (ch.name.startsWith("dm-") || ch.name.startsWith("dm_")) "dm" else ch.type
                    put(COL_CH_TYPE, effectiveType)
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
            // Read existing cached timestamps to prevent overwriting with 0L
            val existingTimestamps = mutableMapOf<String, Long>()
            try {
                val cur = db.rawQuery("SELECT $COL_FRIEND_ID, $COL_FRIEND_HANDLE, $COL_FRIEND_LAST_TIMESTAMP FROM $TABLE_FRIENDS", null)
                cur.use {
                    val idCol = it.getColumnIndex(COL_FRIEND_ID)
                    val hCol = it.getColumnIndex(COL_FRIEND_HANDLE)
                    val tsCol = it.getColumnIndex(COL_FRIEND_LAST_TIMESTAMP)
                    while (it.moveToNext()) {
                        val id = if (idCol >= 0) it.getString(idCol)?.lowercase() else null
                        val h = if (hCol >= 0) it.getString(hCol)?.lowercase()?.removePrefix("@") else null
                        val ts = if (tsCol >= 0) it.getLong(tsCol) else 0L
                        if (ts > 0L) {
                            if (!id.isNullOrBlank()) existingTimestamps[id] = ts
                            if (!h.isNullOrBlank()) existingTimestamps[h] = ts
                        }
                    }
                }
            } catch (_: Exception) {}

            db.delete(TABLE_FRIENDS, null, null)
            val uniqueFriends = friends.distinctBy { it.handle.trim().lowercase().removePrefix("@") }
            for (f in uniqueFriends) {
                val cleanH = f.handle.trim().lowercase().removePrefix("@")
                val existingTs = maxOf(existingTimestamps[f.id.lowercase()] ?: 0L, existingTimestamps[cleanH] ?: 0L)
                val effectiveTs = maxOf(f.lastMessageTimestamp, existingTs)
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
                    put(COL_FRIEND_BANNER, f.bannerUrl)
                    put(COL_FRIEND_LAST_TIMESTAMP, effectiveTs)
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
                val bannerCol = it.getColumnIndex(COL_FRIEND_BANNER)
                val bannerUrl = if (bannerCol >= 0) it.getString(bannerCol) else null
                val tsCol = it.getColumnIndex(COL_FRIEND_LAST_TIMESTAMP)
                val lastTs = if (tsCol >= 0) it.getLong(tsCol) else 0L
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
                        avatarUrl = avatarUrl,
                        bannerUrl = bannerUrl,
                        lastMessageTimestamp = lastTs
                    )
                )
            }
        }
        return list.distinctBy { it.handle.trim().lowercase().removePrefix("@") }
    }

    fun saveGroups(groups: List<FriendItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Read existing cached timestamps to prevent regression
            val existingTimestamps = mutableMapOf<String, Long>()
            try {
                val cur = db.rawQuery("SELECT $COL_GROUP_ID, $COL_GROUP_LAST_TIMESTAMP FROM $TABLE_GROUPS", null)
                cur.use {
                    val idCol = it.getColumnIndex(COL_GROUP_ID)
                    val tsCol = it.getColumnIndex(COL_GROUP_LAST_TIMESTAMP)
                    while (it.moveToNext()) {
                        val id = if (idCol >= 0) it.getString(idCol) else null
                        val ts = if (tsCol >= 0) it.getLong(tsCol) else 0L
                        if (!id.isNullOrBlank() && ts > 0L) {
                            existingTimestamps[id] = ts
                        }
                    }
                }
            } catch (_: Exception) {}

            db.delete(TABLE_GROUPS, null, null)
            val uniqueGroups = groups.distinctBy { it.id }
            for (g in uniqueGroups) {
                val existingTs = existingTimestamps[g.id] ?: 0L
                val effectiveTs = maxOf(g.lastMessageTimestamp, existingTs)
                val membersJson = try {
                    val arr = JSONArray()
                    g.members.forEach { arr.put(it) }
                    arr.toString()
                } catch (_: Exception) { "[]" }

                val cv = ContentValues().apply {
                    put(COL_GROUP_ID, g.id)
                    put(COL_GROUP_NAME, g.name)
                    put(COL_GROUP_HANDLE, g.handle)
                    put(COL_GROUP_INITIAL, g.initial)
                    put(COL_GROUP_STATUS, g.status)
                    put(COL_GROUP_BIO, g.bio)
                    put(COL_GROUP_LAST_MSG, g.lastMessage)
                    put(COL_GROUP_TIME, g.timeAgo)
                    put(COL_GROUP_UNREAD, g.unreadCount)
                    put(COL_GROUP_AVATAR, g.avatarUrl)
                    put(COL_GROUP_IS_CREATED, if (g.isCreatedByMe) 1 else 0)
                    put(COL_GROUP_MEMBER_COUNT, g.memberCount)
                    put(COL_GROUP_MEMBERS_JSON, membersJson)
                    put(COL_GROUP_ADMIN, g.admin)
                    put(COL_GROUP_CREATOR, g.creator)
                    put(COL_GROUP_LAST_TIMESTAMP, effectiveTs)
                }
                db.insertWithOnConflict(TABLE_GROUPS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCachedGroups(): List<FriendItem> {
        val db = readableDatabase
        val list = mutableListOf<FriendItem>()
        val cursor = db.query(TABLE_GROUPS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val avatarCol = it.getColumnIndex(COL_GROUP_AVATAR)
                val avatarUrl = if (avatarCol >= 0) it.getString(avatarCol) else null
                val tsCol = it.getColumnIndex(COL_GROUP_LAST_TIMESTAMP)
                val lastTs = if (tsCol >= 0) it.getLong(tsCol) else 0L
                val membersCol = it.getColumnIndex(COL_GROUP_MEMBERS_JSON)
                val members = mutableListOf<String>()
                if (membersCol >= 0) {
                    val mJson = it.getString(membersCol)
                    if (!mJson.isNullOrBlank()) {
                        try {
                            val arr = JSONArray(mJson)
                            for (i in 0 until arr.length()) {
                                members.add(arr.getString(i))
                            }
                        } catch (_: Exception) {}
                    }
                }
                val isCreated = it.getInt(it.getColumnIndexOrThrow(COL_GROUP_IS_CREATED)) == 1
                val mCount = it.getInt(it.getColumnIndexOrThrow(COL_GROUP_MEMBER_COUNT))

                list.add(
                    FriendItem(
                        id = it.getString(it.getColumnIndexOrThrow(COL_GROUP_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_GROUP_NAME)),
                        handle = it.getString(it.getColumnIndexOrThrow(COL_GROUP_HANDLE)),
                        initial = it.getString(it.getColumnIndexOrThrow(COL_GROUP_INITIAL)),
                        status = it.getString(it.getColumnIndexOrThrow(COL_GROUP_STATUS)) ?: "$mCount members",
                        bio = it.getString(it.getColumnIndexOrThrow(COL_GROUP_BIO)) ?: "Connecto Group Chat",
                        lastMessage = it.getString(it.getColumnIndexOrThrow(COL_GROUP_LAST_MSG)) ?: "Group Chat • Tap to open and talk",
                        timeAgo = it.getString(it.getColumnIndexOrThrow(COL_GROUP_TIME)) ?: "Active",
                        unreadCount = it.getInt(it.getColumnIndexOrThrow(COL_GROUP_UNREAD)),
                        isOnline = false,
                        avatarUrl = avatarUrl,
                        isGroup = true,
                        isCreatedByMe = isCreated,
                        memberCount = mCount,
                        members = members,
                        admin = it.getString(it.getColumnIndexOrThrow(COL_GROUP_ADMIN)) ?: "",
                        creator = it.getString(it.getColumnIndexOrThrow(COL_GROUP_CREATOR)) ?: "",
                        lastMessageTimestamp = lastTs
                    )
                )
            }
        }
        return list
    }

    fun updateGroupLastMessage(groupId: String, lastMessage: String, timeAgo: String, timestampMillis: Long) {
        try {
            val db = writableDatabase
            val cv = ContentValues().apply {
                put(COL_GROUP_LAST_MSG, lastMessage)
                put(COL_GROUP_TIME, timeAgo)
                if (timestampMillis > 0L) {
                    put(COL_GROUP_LAST_TIMESTAMP, timestampMillis)
                }
            }
            db.update(TABLE_GROUPS, cv, "$COL_GROUP_ID = ?", arrayOf(groupId))
        } catch (_: Exception) {}
    }

    /**
     * Permanently purges a group, its channel entry, and all its cached messages from local SQLite.
     * Guarantees 0ms local deletion and prevents any resurrection on app restarts.
     */
    fun deleteCachedGroup(groupId: String) {
        if (groupId.isBlank()) return
        try {
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete(TABLE_GROUPS, "$COL_GROUP_ID = ?", arrayOf(groupId))
                db.delete(TABLE_CHANNELS, "$COL_CH_ID = ?", arrayOf(groupId))
                db.delete(TABLE_MESSAGES, "$COL_MSG_CHANNEL = ?", arrayOf(groupId))
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (_: Exception) {}
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

    fun updateFriendLastMessageAndUnread(
        friendIdOrHandle: String,
        lastMessage: String,
        timeAgo: String,
        unreadCount: Int,
        lastMessageTimestamp: Long = 0L
    ) {
        try {
            val db = writableDatabase
            val clean = friendIdOrHandle.trim().lowercase().removePrefix("@")
            val cleanBase = clean.replace(Regex("\\d+$"), "")
            val cv = ContentValues().apply {
                put(COL_FRIEND_UNREAD, unreadCount)
                put(COL_FRIEND_LAST_MSG, lastMessage)
                put(COL_FRIEND_TIME, timeAgo)
                if (lastMessageTimestamp > 0L) {
                    put(COL_FRIEND_LAST_TIMESTAMP, lastMessageTimestamp)
                }
            }
            db.update(
                TABLE_FRIENDS,
                cv,
                "LOWER($COL_FRIEND_ID) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_NAME) = ? OR ($COL_FRIEND_HANDLE LIKE ? AND ? != '')",
                arrayOf(clean, clean, "@$clean", clean, "%$cleanBase%", cleanBase)
            )
        } catch (_: Exception) {}
    }

    fun updateFriendUnreadCount(friendIdOrHandle: String, unreadCount: Int = 0) {
        try {
            val db = writableDatabase
            val clean = friendIdOrHandle.trim().lowercase().removePrefix("@")
            val cv = ContentValues().apply {
                put(COL_FRIEND_UNREAD, unreadCount)
            }
            db.update(
                TABLE_FRIENDS,
                cv,
                "LOWER($COL_FRIEND_ID) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_NAME) = ?",
                arrayOf(clean, clean, "@$clean", clean)
            )
        } catch (_: Exception) {}
    }

    fun incrementFriendUnreadCount(
        friendIdOrHandle: String,
        lastMessage: String? = null,
        timeAgo: String? = null,
        lastMessageTimestamp: Long = 0L
    ): Int {
        try {
            val db = writableDatabase
            val clean = friendIdOrHandle.trim().lowercase().removePrefix("@")
            val cleanBase = clean.replace(Regex("\\d+$"), "")

            var friendId: String? = null
            var currentUnread = 0

            val cursor = db.rawQuery(
                """
                SELECT $COL_FRIEND_ID, $COL_FRIEND_UNREAD 
                FROM $TABLE_FRIENDS 
                WHERE LOWER($COL_FRIEND_ID) = ? 
                   OR LOWER($COL_FRIEND_HANDLE) = ? 
                   OR LOWER($COL_FRIEND_HANDLE) = ? 
                   OR LOWER($COL_FRIEND_NAME) = ?
                   OR ($COL_FRIEND_HANDLE LIKE ? AND ? != '')
                LIMIT 1
                """.trimIndent(),
                arrayOf(clean, clean, "@$clean", clean, "%$cleanBase%", cleanBase)
            )
            cursor.use {
                if (it.moveToFirst()) {
                    friendId = it.getString(0)
                    currentUnread = it.getInt(1)
                }
            }

            val newUnread = currentUnread + 1
            val cv = ContentValues().apply {
                put(COL_FRIEND_UNREAD, newUnread)
                if (!lastMessage.isNullOrBlank()) put(COL_FRIEND_LAST_MSG, lastMessage)
                if (!timeAgo.isNullOrBlank()) put(COL_FRIEND_TIME, timeAgo)
                if (lastMessageTimestamp > 0L) put(COL_FRIEND_LAST_TIMESTAMP, lastMessageTimestamp)
            }

            if (friendId != null) {
                db.update(TABLE_FRIENDS, cv, "$COL_FRIEND_ID = ?", arrayOf(friendId))
            } else {
                db.update(
                    TABLE_FRIENDS,
                    cv,
                    "LOWER($COL_FRIEND_ID) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_NAME) = ?",
                    arrayOf(clean, clean, "@$clean", clean)
                )
            }
            return newUnread
        } catch (_: Exception) {
            return 1
        }
    }

    fun getFriendByUsernameOrHandle(identifier: String): FriendItem? {
        try {
            val db = readableDatabase
            val clean = identifier.trim().lowercase().removePrefix("@")
            val cleanBase = clean.replace(Regex("\\d+$"), "")
            val cursor = db.rawQuery(
                """
                SELECT * FROM $TABLE_FRIENDS 
                WHERE LOWER($COL_FRIEND_ID) = ? 
                   OR LOWER($COL_FRIEND_HANDLE) = ? 
                   OR LOWER($COL_FRIEND_HANDLE) = ? 
                   OR LOWER($COL_FRIEND_NAME) = ?
                   OR ($COL_FRIEND_HANDLE LIKE ? AND ? != '')
                LIMIT 1
                """.trimIndent(),
                arrayOf(clean, clean, "@$clean", clean, "%$cleanBase%", cleanBase)
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val avatarCol = it.getColumnIndex(COL_FRIEND_AVATAR)
                    val avatarUrl = if (avatarCol >= 0) it.getString(avatarCol) else null
                    val bannerCol = it.getColumnIndex(COL_FRIEND_BANNER)
                    val bannerUrl = if (bannerCol >= 0) it.getString(bannerCol) else null
                    val tsCol = it.getColumnIndex(COL_FRIEND_LAST_TIMESTAMP)
                    val lastTs = if (tsCol >= 0) it.getLong(tsCol) else 0L
                    return FriendItem(
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
                        avatarUrl = avatarUrl,
                        bannerUrl = bannerUrl,
                        lastMessageTimestamp = lastTs
                    )
                }
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Resolves the latest message content and timestamp for a friend conversation by searching
     * cached messages in direct channels / allowed authors.
     */
    fun getLatestMessageInfoForFriend(myUsername: String, friendHandleOrId: String): Pair<String, Long>? {
        return try {
            val msgs = getDirectMessages(myUsername, friendHandleOrId, limit = 5)
            val latest = msgs.lastOrNull() ?: return null
            val content = latest.content
            val ts = parseIsoEpochMillis(latest.createdAt) ?: 0L
            Pair(content, ts)
        } catch (_: Exception) {
            null
        }
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

    /** Permanently wipe all mutual local cached data (messages, channels, friend entry) between two users. */
    fun deleteMutualConversationData(currentUsername: String, friendUsername: String) {
        val db = writableDatabase
        val u1 = currentUsername.trim().lowercase().removePrefix("@")
        val u2 = friendUsername.trim().lowercase().removePrefix("@")
        if (u1.isEmpty() || u2.isEmpty()) return

        val sortedPair = listOf(u1, u2).sorted()
        val canonDash = "dm-${sortedPair[0]}-${sortedPair[1]}"
        val canonUnderscore = "dm_${sortedPair[0]}_${sortedPair[1]}"
        val dir1 = "dm-$u1-$u2"
        val dir2 = "dm-$u2-$u1"
        val dir3 = "dm_${u1}_${u2}"
        val dir4 = "dm_${u2}_${u1}"

        db.beginTransaction()
        try {
            val channelArgs = arrayOf(canonDash, canonUnderscore, dir1, dir2, dir3, dir4)
            val placeholders = channelArgs.joinToString(",") { "?" }
            db.delete(TABLE_MESSAGES, "$COL_MSG_CHANNEL IN ($placeholders)", channelArgs)
            db.delete(TABLE_MESSAGES, "$COL_MSG_CHANNEL LIKE ? AND $COL_MSG_CHANNEL LIKE ?", arrayOf("%$u1%", "%$u2%"))
            db.delete(TABLE_CHANNELS, "$COL_CH_ID IN ($placeholders) OR $COL_CH_NAME IN ($placeholders)", arrayOf(*channelArgs, *channelArgs))
            db.delete(
                TABLE_FRIENDS,
                "LOWER($COL_FRIEND_ID) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_HANDLE) = ? OR LOWER($COL_FRIEND_NAME) = ?",
                arrayOf(u2, u2, "@$u2", u2)
            )
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun clearAllCache() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_MESSAGES, null, null)
            db.delete(TABLE_CHANNELS, null, null)
            db.delete(TABLE_FRIENDS, null, null)
            db.delete(TABLE_GROUPS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
