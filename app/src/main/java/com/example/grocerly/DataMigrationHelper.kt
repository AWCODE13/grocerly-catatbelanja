package com.example.grocerly.utils

import android.content.Context
import com.example.grocerly.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DataMigrationHelper {

    fun migrateGuestToUser(
        context: Context,
        userEmail: String
    ) {
        val db = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            val database = db.openHelper.writableDatabase

            database.beginTransaction()
            try {
                database.execSQL(
                    "UPDATE list_belanja SET ownerEmail = ? WHERE ownerEmail = 'guest'",
                    arrayOf(userEmail)
                )
                database.execSQL(
                    "UPDATE item_belanja SET ownerEmail = ? WHERE ownerEmail = 'guest'",
                    arrayOf(userEmail)
                )
                database.execSQL(
                    "UPDATE purchase SET ownerEmail = ? WHERE ownerEmail = 'guest'",
                    arrayOf(userEmail)
                )

                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
        }
    }
}
