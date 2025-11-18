package com.example.sportsorganizer.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class CompetitionWithConfig(
    @Embedded val competition: Competition,
    @Relation(parentColumn = "id", entityColumn = "competition_id", entity = CompetitionConfig::class)
    val config: CompetitionConfig?
)

