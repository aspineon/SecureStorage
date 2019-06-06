package com.ehsanmashhadi.securestorage

import android.util.Base64

class EncodingUtil {

    companion object {

        fun bytesToBase64(bytes: ByteArray): String {

            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        fun base64ToBytes(base64: String): ByteArray {

            return Base64.decode(base64, Base64.DEFAULT)
        }
    }
}