package com.ehsanmashhadi.securestorage

import java.nio.charset.Charset
import java.security.Key
import java.security.KeyFactory
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


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

        fun derivateKey(pin: ByteArray, salt: ByteArray, pbeAlgorithm:String, encryptionAlgorithm:String,
                        iterationNo:Int, keySize:Int): SecretKey{

            val factory = SecretKeyFactory.getInstance(pbeAlgorithm)
            val pinCharArray = String(pin, Charset.forName("UTF-8")).toCharArray()
            val spec = PBEKeySpec(pinCharArray, salt, iterationNo, keySize)
            val secret = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(secret.encoded,encryptionAlgorithm)
            return secretKey
        }
    }
}