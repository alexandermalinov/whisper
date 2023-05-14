package com.example.whisper.data.local.entity

import androidx.room.*

@Entity(
    tableName = "user_and_contact_relation_table",
    primaryKeys = ["user_id", "contact_url"]
)
data class UserAndContactCrossRef(
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "contact_url")
    val contactUrl: String
)

data class UserAndContact(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "user_id",
        entity = Contact::class,
        entityColumn = "contact_url",
        associateBy = Junction(UserAndContactCrossRef::class)
    )
    val contacts: List<Contact>
)