package com.nexus.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, NoteLink::class], version = 1, exportSchema = false)
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
                ).addCallback(object : RoomDatabase.Callback() {
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

            val n1 = dao.insertNote(Note(title = "Proyecto NEXUS", content = "Una libreta que piensa en red: notas, enlaces y un mapa visual de todo.", createdAt = now - 1 * day))
            val n2 = dao.insertNote(Note(title = "Ideas sueltas", content = "Un cajón sin orden para todo lo que aún no tiene forma.", createdAt = now))
            val n3 = dao.insertNote(Note(title = "Lectura activa", content = "Subrayar es fácil, reformular es lo que deja huella. Reescribe con tus palabras.", createdAt = now - 2 * day))
            val n4 = dao.insertNote(Note(title = "Escritura diaria", content = "Diez minutos cada mañana. Sin editar, sin juzgar. Solo capturar.", createdAt = now - 1 * day))
            val n5 = dao.insertNote(Note(title = "Grafo de conocimiento", content = "Las ideas no viven solas. Cada nota es un nodo y cada enlace una sinapsis que le da contexto.", createdAt = now - 2 * day))

            dao.insertLink(NoteLink(fromNoteId = n1, toNoteId = n5))
            dao.insertLink(NoteLink(fromNoteId = n1, toNoteId = n4))
            dao.insertLink(NoteLink(fromNoteId = n1, toNoteId = n3))
            dao.insertLink(NoteLink(fromNoteId = n1, toNoteId = n2))
            dao.insertLink(NoteLink(fromNoteId = n5, toNoteId = n3))
            dao.insertLink(NoteLink(fromNoteId = n4, toNoteId = n3))

            NexusRepository(dao).autoLinkAllNotes()
        }
    }
}
