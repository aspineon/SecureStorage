package com.ehsanmashhadi.securestorage

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal


class KeyStoreUtil {

    companion object {

        private const val AndroidKeyStore = "AndroidKeyStore"
        private val keyStore = KeyStore.getInstance(AndroidKeyStore)

        @RequiresApi(Build.VERSION_CODES.M)
        fun generateSymmetricKey(keyAlias: String): Key {

            keyStore.load(null)
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)
                val keyGenParameterSpec =
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                keyGenerator.init(keyGenParameterSpec)
                return keyGenerator.generateKey()
            }
            return keyStore.getKey(keyAlias, null)
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        fun generateAsymmetricKey(context: Context, keyAlias: String): KeyStore.Entry? {

            keyStore.load(null)
            if (!keyStore.containsAlias(keyAlias)) {
                val startDate = Calendar.getInstance()
                var endDate = Calendar.getInstance()
                endDate.add(Calendar.YEAR, 25)
                val keyPairGeneratorSpec = KeyPairGeneratorSpec.Builder(context.applicationContext)
                    .setAlias(keyAlias).setSubject(X500Principal("CN=" + keyAlias))
                    .setSerialNumber(BigInteger.valueOf(123456))
                    .setStartDate(startDate.time)
                    .setEndDate(endDate.time)
                    .build()
                val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore)
                keyPairGenerator.initialize(keyPairGeneratorSpec)
                keyPairGenerator.generateKeyPair()
            }
            return keyStore.getEntry(keyAlias, null)
        }

        fun getKey(keyAlias: String): Key? {

            keyStore.load(null)
            if (keyStore.containsAlias(keyAlias)) {
                return keyStore.getKey(keyAlias, null)
            }
            return null
        }

        fun getKeyEntry(keyAlias: String): KeyStore.Entry? {

            keyStore.load(null)
            if (keyStore.containsAlias(keyAlias)) {
                return keyStore.getEntry(keyAlias, null)
            }
            return null
        }

        fun deleteEntries() {
            keyStore.load(null)
            for (alias in keyStore.aliases()) {
                keyStore.deleteEntry(alias)
            }
        }
    }
}