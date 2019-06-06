package com.ehsanmashhadi.securestorage

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    private lateinit var mRadioGroupMethod: RadioGroup
    private lateinit var mRadioButtonAsymmetric: RadioButton
    private lateinit var mRadioButtonSymmetric: RadioButton
    private lateinit var mRadioButtonPbkdf: RadioButton
    private lateinit var mRelativeLayoutSpecs: RelativeLayout
    private lateinit var mEditTextPin: EditText
    private lateinit var mEditTextKeyAlias: EditText
    private lateinit var mEditTextPlainText: EditText
    private lateinit var mEditTextCipherText: EditText
    private lateinit var mButtonGenerateKey: Button
    private lateinit var mButtonEncrypt: Button
    private lateinit var mButtonDecrypt: Button
    private lateinit var mTextViewResult: TextView
    private lateinit var mSharedPreference: SharedPreferences

    companion object {
        const val SHARED_PREFERENCE_KEY = "aes_encrypted"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUiComponents()
        initListeners()
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
    }

    private fun initUiComponents() {

        mRadioGroupMethod = findViewById(R.id.radiogroup_mainactivity_method)
        mRadioButtonAsymmetric = findViewById(R.id.radiobutton_mainactivity_asymmetric)
        mRadioButtonSymmetric = findViewById(R.id.radiobutton_mainactivity_symmetric)
        mRadioButtonPbkdf = findViewById(R.id.radiobutton_mainactivity_pbkdf)
        mRelativeLayoutSpecs = findViewById(R.id.relativelayout_mainactivity_specs)
        mEditTextPin = findViewById(R.id.edittext_mainactivity_pin)
        mEditTextKeyAlias = findViewById(R.id.edittext_mainactivity_keyalias)
        mEditTextPlainText = findViewById(R.id.edittext_mainactivity_plaintext)
        mEditTextCipherText = findViewById(R.id.edittext_mainactivity_ciphertext)
        mButtonGenerateKey = findViewById(R.id.button_mainactivity_generatekey)
        mButtonEncrypt = findViewById(R.id.button_mainactivity_encrypt)
        mButtonDecrypt = findViewById(R.id.button_mainactivity_decrypt)
        mTextViewResult = findViewById(R.id.textview_mainactivity_result)
    }

    private fun initListeners() {

        mButtonGenerateKey.setOnClickListener { generateKey() }
        mButtonEncrypt.setOnClickListener { encrypt() }
        mButtonDecrypt.setOnClickListener { decrypt() }

        mRadioGroupMethod.setOnCheckedChangeListener { _, checkedId ->

            KeyStoreUtil.deleteEntries()

            when (checkedId) {

                R.id.radiobutton_mainactivity_asymmetric -> {
                    mEditTextPin.visibility = View.GONE
                    if (Build.VERSION.SDK_INT < 18) {
                        mRelativeLayoutSpecs.visibility = View.GONE
                    }
                }

                R.id.radiobutton_mainactivity_symmetric -> {
                    mEditTextPin.visibility = View.GONE
                    if (Build.VERSION.SDK_INT < 23) {
                        mRelativeLayoutSpecs.visibility = View.GONE
                    }
                }

                R.id.radiobutton_mainactivity_pbkdf -> {
                    mEditTextPin.visibility = View.VISIBLE
                    mRelativeLayoutSpecs.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun generateKey() {

        if (mRadioButtonSymmetric.isChecked && Build.VERSION.SDK_INT >= 23) {

            KeyStoreUtil.generateSymmetricKey(mEditTextKeyAlias.text.toString())
        } else if (mRadioButtonAsymmetric.isChecked && Build.VERSION.SDK_INT >= 18) {

            val privateKey = KeyStoreUtil.generateAsymmetricKey(
                applicationContext,
                mEditTextKeyAlias.text.toString()
            ) as KeyStore.PrivateKeyEntry

            val randomKey = CryptoUtil.generateSecureRandom(16)
            val encryptedAesKey = CryptoUtil.encrypt(
                CryptoUtil.CIPHER_RSA,
                randomKey, privateKey.certificate.publicKey
            )
            mSharedPreference.edit().putString(SHARED_PREFERENCE_KEY, EncodingUtil.bytesToBase64(encryptedAesKey!!))
                .commit()

        } else if (mRadioButtonPbkdf.isChecked) {
            //PBKDF
        }

    }

    private fun encrypt() {

        if (mRadioButtonSymmetric.isChecked) {

            val cipherText = CryptoUtil.encrypt(
                CryptoUtil.CIPHER_AES,
                mEditTextPlainText.text.toString().toByteArray(Charsets.UTF_8),
                KeyStoreUtil.getKey(mEditTextKeyAlias.text.toString())!!
            )
            mTextViewResult.text = if (cipherText != null) EncodingUtil
                .bytesToBase64(cipherText) else "NPE"

        } else if (mRadioButtonAsymmetric.isChecked) {

            val encryptedKey = EncodingUtil.base64ToBytes(
                mSharedPreference.getString(
                    SHARED_PREFERENCE_KEY
                    , ""
                )!!
            )
            val asymmetricKey = KeyStoreUtil.getKeyEntry(mEditTextKeyAlias.text.toString()) as KeyStore.PrivateKeyEntry
            val aesKey = CryptoUtil.decrypt(CryptoUtil.CIPHER_RSA, encryptedKey, asymmetricKey.privateKey)
            val cipherText = CryptoUtil.encrypt(
                CryptoUtil.CIPHER_AES,
                mEditTextPlainText.text.toString().toByteArray(Charsets.UTF_8),
                SecretKeySpec(aesKey, "AES")
            )
            mTextViewResult.text = if (cipherText != null) EncodingUtil
                .bytesToBase64(cipherText) else "NPE"
        }
    }

    private fun decrypt() {

        if (mRadioButtonSymmetric.isChecked) {

            val plainText = CryptoUtil.decrypt(
                CryptoUtil.CIPHER_AES,
                EncodingUtil.base64ToBytes(mEditTextCipherText.text.toString()),
                (KeyStoreUtil.getKey(mEditTextKeyAlias.text.toString())!!)
            )
            mTextViewResult.text = if (plainText != null) String(plainText) else "NPE"
        } else if (mRadioButtonAsymmetric.isChecked) {

            val encryptedKey = EncodingUtil.base64ToBytes(
                mSharedPreference.getString(
                    SHARED_PREFERENCE_KEY
                    , ""
                )!!
            )

            val asymmetricKey = KeyStoreUtil.getKeyEntry(mEditTextKeyAlias.text.toString()) as KeyStore.PrivateKeyEntry
            val aesKey = CryptoUtil.decrypt(CryptoUtil.CIPHER_RSA, encryptedKey, asymmetricKey.privateKey)

            val plainText = CryptoUtil.decrypt(
                CryptoUtil.CIPHER_AES,
                EncodingUtil.base64ToBytes(mEditTextCipherText.text.toString()),
                SecretKeySpec(aesKey, "AES")
            )
            mTextViewResult.text = if (plainText != null) String(plainText) else "NPE"
        }
    }
}