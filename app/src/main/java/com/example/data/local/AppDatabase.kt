package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.SkipRecord
import com.example.data.model.VipMembership
import com.example.data.preset.VipPresetsCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VipMembership::class, SkipRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vipMembershipDao(): VipMembershipDao
    abstract fun skipRecordDao(): SkipRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skip_vip_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with default starter presets
                            CoroutineScope(Dispatchers.IO).launch {
                                val starterPresets = listOf(
                                    VipPresetsCatalog.presets[0].toVipMembership(), // Fabletics ($69.95)
                                    VipPresetsCatalog.presets[1].toVipMembership(), // Savage X Fenty ($49.95)
                                    VipPresetsCatalog.presets[2].toVipMembership()  // JustFab ($49.95)
                                )
                                getDatabase(context).vipMembershipDao().insertAll(starterPresets)
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Ensure existing memberships on device update to 69.95
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    db.execSQL("UPDATE vip_memberships SET monthlyCost = 69.95 WHERE name = 'Fabletics' OR monthlyCost = 59.95")
                                } catch (_: Exception) {}
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
