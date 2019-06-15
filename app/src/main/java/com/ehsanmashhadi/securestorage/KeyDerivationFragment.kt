package com.ehsanmashhadi.securestorage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import javax.crypto.SecretKey

class KeyDerivationFragment : Fragment() {

    private lateinit var mViewRoot: View
    private lateinit var mEditTextPlainText: EditText
    private lateinit var mEditTextCipherText: EditText
    private lateinit var mEditTextPin: EditText
    private lateinit var mButtonEncrypt: Button
    private lateinit var mButtonDecrypt: Button
    private lateinit var mButtonDerivate: Button
    private lateinit var mSharedPreference: SharedPreferences
    private lateinit var mCallBack: CallBackInterface
    private lateinit var mSecretKey: SecretKey

    companion object {
        const val SHARED_PREFERENCE_SALT = "salt"
        const val SHARED_PREFERENCE_DIGEST = "digest"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewRoot = inflater.inflate(R.layout.fragment_keyderivate, null)
        return mViewRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiComponents()
        initListeners()
    }

    private fun initUiComponents() {

        mEditTextPlainText = mViewRoot.findViewById(R.id.edittext_layout_plaintext)
        mEditTextPin = mViewRoot.findViewById(R.id.edittext_layout_pin)
        mEditTextCipherText = mViewRoot.findViewById(R.id.edittext_layout_ciphertext)
        mButtonEncrypt = mViewRoot.findViewById(R.id.button_layout_encrypt)
        mButtonDecrypt = mViewRoot.findViewById(R.id.button_layout_decrypt)
        mButtonDerivate = mViewRoot.findViewById(R.id.button_layout_derivate)
        mButtonEncrypt.isEnabled = false
        mButtonDecrypt.isEnabled = false
        checkStatus()
    }

    private fun checkStatus() {

        mSharedPreference.getString(SHARED_PREFERENCE_DIGEST, null)?.let {
            mEditTextPin.hint = "Insert Previous Pin"
        } ?: run {
            mEditTextPin.hint = "Insert Pin"
        }
    }

    private fun initListeners() {
        mButtonDerivate.setOnClickListener {
            derivateKey()
            checkStatus()
        }

        mButtonEncrypt.setOnClickListener { encrypt() }
        mButtonDecrypt.setOnClickListener { decrypt() }
    }

    private fun derivateKey() {

        lateinit var salt: ByteArray

        mSharedPreference.getString(SHARED_PREFERENCE_DIGEST, null)?.let {
            val digest =
                EncodingUtil.bytesToBase64(CryptoUtil.sha1(mEditTextPin.text.toString().toByteArray(Charsets.UTF_8)))
            if (digest.trim() == it.trim()) {
                salt = EncodingUtil.base64ToBytes(mSharedPreference.getString(SHARED_PREFERENCE_SALT, null)!!)
                Toast.makeText(context, "You entered successfully!", Toast.LENGTH_LONG).show()
                mSecretKey = CryptoUtil.derivateKey(
                    mEditTextPin.text.toString().toByteArray(Charsets.UTF_8),
                    salt,
                    CryptoUtil.PBKDF2_HMAC_SHA1,
                    CryptoUtil.ENCRYPTION_ALGORITHM,
                    CryptoUtil.ROUND,
                    CryptoUtil.LENGTH
                )
                mButtonEncrypt.isEnabled = true
                mButtonDecrypt.isEnabled = true
            } else {
                Toast.makeText(context, "Wrong! Try again!", Toast.LENGTH_LONG).show()
                mButtonEncrypt.isEnabled = false
                mButtonDecrypt.isEnabled = false
            }

        } ?: kotlin.run {
            salt = CryptoUtil.generateSecureRandom(16)
            mSecretKey = CryptoUtil.derivateKey(
                mEditTextPin.text.toString().toByteArray(Charsets.UTF_8),
                salt,
                CryptoUtil.PBKDF2_HMAC_SHA1,
                CryptoUtil.ENCRYPTION_ALGORITHM,
                CryptoUtil.ROUND,
                CryptoUtil.LENGTH
            )
            val digest = CryptoUtil.sha1(mEditTextPin.text.toString().toByteArray(Charsets.UTF_8))
            val sharedPreferenceEditor = mSharedPreference.edit()
            sharedPreferenceEditor.putString(SHARED_PREFERENCE_SALT, EncodingUtil.bytesToBase64(salt))
            sharedPreferenceEditor.putString(SHARED_PREFERENCE_DIGEST, EncodingUtil.bytesToBase64(digest))

            if (sharedPreferenceEditor.commit()) {
                Toast.makeText(context, "Key generated successfully!", Toast.LENGTH_LONG).show()
                mEditTextPin.setText("")
            } else {
                Toast.makeText(context, "Key generation failed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun encrypt() {

        val cipherText = CryptoUtil.encrypt(
            CryptoUtil.CIPHER_AES,
            mEditTextPlainText.text.toString().toByteArray(Charsets.UTF_8),
            mSecretKey
        )

        cipherText?.let {
            mCallBack.setResult(EncodingUtil.bytesToBase64(cipherText))
        } ?: run {
            mCallBack.setResult("NPE")
        }
    }

    private fun decrypt() {
        val plainText = CryptoUtil.decrypt(
            CryptoUtil.CIPHER_AES,
            EncodingUtil.base64ToBytes(mEditTextCipherText.text.toString()),
            mSecretKey
        )
        plainText?.let {
            mCallBack.setResult(String(plainText))
        } ?: run {
            mCallBack.setResult("NPE")
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity is CallBackInterface) {
            mCallBack = activity as CallBackInterface
        } else {
            throw ClassCastException(activity.toString() + " must implement " + CallBackInterface::class.java.canonicalName)
        }
    }
}