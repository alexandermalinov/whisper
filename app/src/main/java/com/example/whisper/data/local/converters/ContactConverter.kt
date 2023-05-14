package com.example.whisper.data.local.converters

import com.example.whisper.data.local.entity.Contact
import androidx.room.TypeConverter
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ContactTypeConverter {

    @TypeConverter
    fun List<Contact>.listOfContactsToJson() = Json.encodeToString(this)

    @TypeConverter
    fun String.jsonToListOfContacts() = Json.decodeFromString<List<Contact>>(this)
}