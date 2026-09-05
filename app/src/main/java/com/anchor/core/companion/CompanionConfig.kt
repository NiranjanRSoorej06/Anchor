package com.anchor.core.companion

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CompanionContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String
)

class CompanionPreferences(context: Context) {
    private companion object {
        const val PREF_NAME = "anchor_companion_prefs"
        const val KEY_ENABLED = "companion_mode_enabled"
        const val KEY_NOTIFY_ON_SOS_START = "companion_notify_on_sos_start"
        const val KEY_SHARE_LOCATION = "companion_share_location"
        const val KEY_CONTACTS_JSON = "companion_contacts_json"
        const val DEFAULT_MESSAGE = "Anchor Alert: They may need a quiet moment."
        const val SOS_START_MESSAGE = "Anchor Alert: They just hit their SOS button and might want a check-in."
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /**
     * Whether trusted contacts are notified the moment the user hits SOS,
     * not just if they are still distressed at the end. Defaults to
     * following [isEnabled] until the user explicitly overrides it.
     */
    var notifyOnSosStart: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ON_SOS_START, isEnabled)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ON_SOS_START, value).apply()

    fun getSosStartMessageText(): String = SOS_START_MESSAGE

    /**
     * Whether the trusted-contact alert includes a Maps link to the
     * user's last-known location. Defaults to on — this is the behavior
     * the user asked for — but stays an explicit, revocable toggle
     * (surfaced in [com.anchor.ui.companion.CompanionModeScreen]) since
     * location is more sensitive than the alert text alone.
     */
    var shareLocationOnAlert: Boolean
        get() = prefs.getBoolean(KEY_SHARE_LOCATION, true)
        set(value) = prefs.edit().putBoolean(KEY_SHARE_LOCATION, value).apply()

    fun getContacts(): List<CompanionContact> {
        val jsonStr = prefs.getString(KEY_CONTACTS_JSON, null) ?: return emptyList()
        val list = mutableListOf<CompanionContact>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CompanionContact(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        phoneNumber = obj.optString("phoneNumber", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addContact(name: String, phoneNumber: String): CompanionContact {
        val contacts = getContacts().toMutableList()
        val newContact = CompanionContact(name = name, phoneNumber = phoneNumber)
        contacts.add(newContact)
        saveContacts(contacts)
        return newContact
    }

    fun removeContact(id: String) {
        val contacts = getContacts().filter { it.id != id }
        saveContacts(contacts)
    }

    private fun saveContacts(contacts: List<CompanionContact>) {
        val array = JSONArray()
        for (contact in contacts) {
            val obj = JSONObject().apply {
                put("id", contact.id)
                put("name", contact.name)
                put("phoneNumber", contact.phoneNumber)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS_JSON, array.toString()).apply()
    }

    fun getMessageText(): String = DEFAULT_MESSAGE
}
