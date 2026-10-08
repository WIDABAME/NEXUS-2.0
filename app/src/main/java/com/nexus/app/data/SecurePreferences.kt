@file:Suppress("DEPRECATION")

package com.nexus.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de preferencias encriptadas utilizando Jetpack Security (Crypto).
 * Utiliza el hardware del dispositivo (Android Keystore / TEE / StrongBox)
 * para generar y gestionar las claves de cifrado de forma aislada.
 */
object SecurePreferences {

    private const val PREFS_FILENAME = "secure_user_prefs"
    private const val KEY_SECURE_USER_ID = "secure_user_id"

    /**
     * Obtiene una instancia de [SharedPreferences] encriptada con AES-256-SIV para claves
     * y AES-256-GCM para valores.
     */
    fun getEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Guarda de forma segura el ID de usuario cifrado en almacenamiento local.
     */
    fun saveSecureUserId(context: Context, id: String) {
        val prefs = getEncryptedPreferences(context)
        prefs.edit { putString(KEY_SECURE_USER_ID, id) }
    }

    /**
     * Recupera el ID de usuario cifrado.
     * @return El ID descifrado o null si no existe.
     */
    fun getSecureUserId(context: Context): String? {
        val prefs = getEncryptedPreferences(context)
        return prefs.getString(KEY_SECURE_USER_ID, null)
    }

    /**
     * Elimina el ID de usuario cifrado guardado.
     */
    fun clearSecureUserId(context: Context) {
        val prefs = getEncryptedPreferences(context)
        prefs.edit { remove(KEY_SECURE_USER_ID) }
    }
}
