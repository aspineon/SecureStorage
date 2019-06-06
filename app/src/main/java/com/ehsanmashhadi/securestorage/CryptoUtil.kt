package com.ehsanmashhadi.securestorage

import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher


class CryptoUtil {

    companion object {

        const val CIPHER_AES = "AES/ECB/PKCS7Padding"
        const val CIPHER_RSA = "RSA/ECB/PKCS1Padding"

        fun generateSecureRandom(length: Int): ByteArray {
            val random = ByteArray(length)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(random)
            return random
        }

        fun encrypt(cipherAlgorithm: String, plainText: ByteArray, key: Key): ByteArray? {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cipherText = cipher.doFinal(plainText)
            return cipherText
        }

        fun decrypt(cipherAlgorithm: String, cipherText: ByteArray, key: Key): ByteArray? {
            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val plainText = cipher.doFinal(cipherText)
            return plainText
        }
    }
}