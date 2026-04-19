package com.ledger.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,           // "lent" | "borrowed" | "group"
    val personName: String,
    val groupName: String = "",
    val amount: Int,
    val category: String,
    val note: String,
    val date: String,
    val isSettled: Boolean = false
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String,
    val members: String         // comma-separated names
)

@Entity(tableName = "group_expenses")
data class GroupExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val title: String,
    val emoji: String,
    val paidBy: String,
    val amount: Int,
    val splitCount: Int,
    val date: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,  // single user
    val name: String,
    val email: String,
    val initials: String,
    val passwordHash: String = ""
)
