package com.example.sportsorganizer.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class UserWithCompetitions (
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "organizer_id"
    )
    val playlists: List<Competition>
)