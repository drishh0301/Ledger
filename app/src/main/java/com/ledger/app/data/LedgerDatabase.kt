package com.ledger.app.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [TransactionEntity::class, GroupEntity::class, GroupExpenseEntity::class, UserEntity::class],
    version  = 2,
    exportSchema = false
)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao
    abstract fun groupExpenseDao(): GroupExpenseDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: LedgerDatabase? = null

        fun get(context: Context): LedgerDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java, "ledger_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
