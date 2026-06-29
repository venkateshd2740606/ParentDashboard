package com.parentdashboard.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "parentdashboard_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun storeSecureValue(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun getSecureValue(key: String): String? = encryptedPrefs.getString(key, null)

    suspend fun verifyAppIntegrity(nonce: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val manager = IntegrityManagerFactory.create(context)
            val request = IntegrityTokenRequest.builder().setNonce(nonce).build()
            val response = manager.requestIntegrityToken(request)
            Result.success(response.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isRunningOnEmulator(): Boolean =
        android.os.Build.FINGERPRINT.startsWith("generic") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MODEL.contains("google_sdk")

    fun isAppIntegrityValid(): Boolean = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
        true
    } catch (_: Exception) { false }
}
