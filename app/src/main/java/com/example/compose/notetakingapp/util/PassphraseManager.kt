package com.example.compose.notetakingapp.util

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.RegistryConfiguration
import java.io.File
import java.security.SecureRandom

class PassphraseManager(private val context: Context) {

    companion object {
        private const val KEYSET_NAME = "db_passphrase_keyset"
        private const val PREF_FILE_NAME = "secure_prefs"
        private const val MASTER_KEY_URI = "android-keystore://db_master_key"
        private const val PASSPHRASE_FILE = "db_passphrase.bin"
    }

    private val aead: Aead

    init {
        AeadConfig.register()
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    fun getPassphrase(): ByteArray {
        val file = File(context.filesDir, PASSPHRASE_FILE)
        return if (!file.exists()) {
            val newPassphrase = generatePassphrase()
            val encrypted = aead.encrypt(newPassphrase.toByteArray(), null)
            file.writeBytes(encrypted)
            newPassphrase.toByteArray()
        } else {
            val encrypted = file.readBytes()
            aead.decrypt(encrypted, null)
        }
    }

    private fun generatePassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
