package com.nexus.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, NoteLink::class, Checklist::class, ChecklistItem::class], version = 2, exportSchema = false)
abstract class NexusDatabase : RoomDatabase() {

    abstract fun nexusDao(): NexusDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                INSTANCE?.let { seedInitialData(it) }
                            }
                        }
                    }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: NexusDatabase) {
            val dao = db.nexusDao()
            val now = System.currentTimeMillis()
            val day = 24L * 60 * 60 * 1000

            // Seed initial checklist example
            val c1 = dao.insertChecklist(Checklist(title = "Lista de Verificación Nexus", createdAt = now))
            dao.insertChecklistItem(ChecklistItem(checklistId = c1, text = "Crear mi primera nota", isChecked = true, position = 0))
            dao.insertChecklistItem(ChecklistItem(checklistId = c1, text = "Explorar el grafo de conocimiento", isChecked = true, position = 1))
            dao.insertChecklistItem(ChecklistItem(checklistId = c1, text = "Probar las listas chuliables", isChecked = false, position = 2))

            NexusRepository(dao).autoLinkAllNotes()
        }
    }
}
