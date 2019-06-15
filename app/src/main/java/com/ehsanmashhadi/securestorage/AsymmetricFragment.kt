package com.ehsanmashhadi.securestorage

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

class AsymmetricFragment : Fragment() {

    private lateinit var mViewRoot: View
    private lateinit var mEditTextKeyAlias: EditText
    private lateinit var mEditTextPlainText: EditText
    private lateinit var mEditTextCipherText: EditText
    private lateinit var mButtonGenerateKey: Button
    private lateinit var mButtonEncrypt: Button
    private lateinit var mButtonDecrypt: Button
    private lateinit var mSharedPreference: SharedPreferences
    private lateinit var mCallBack: CallBackInterface

    companion object {
        const val SHARED_PREFERENCE_KEY = "aes_encrypted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context?.applicationContext)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewRoot = inflater.inflate(R.layout.fragment_asymmetric, null)
        return mViewRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiComponents()
        initListeners()
    }

    private fun initListeners() {
        if (Build.VERSION.SDK_INT >= 18) {
            mButtonGenerateKey.setOnClickListener {
                generateKey()
            }
        }
        mButtonEncrypt.setOnClickListener { encrypt() }
        mButtonDecrypt.setOnClickListener { decrypt() }
    }

    private fun initUiComponents() {

        mEditTextKeyAlias = mViewRoot.findViewById(R.id.edittext_layout_keyalias)
        mEditTextPlainText = mViewRoot.findViewById(R.id.edittext_layout_plaintext)
        mEditTextCipherText = mViewRoot.findViewById(R.id.edittext_layout_ciphertext)
        mButtonGenerateKey = mViewRoot.findViewById(R.id.button_layout_generatekey)
        mButtonEncrypt = mViewRoot.findViewById(R.id.button_layout_encrypt)
        mButtonDecrypt = mViewRoot.findViewById(R.id.button_layout_decrypt)
        checkStatus()
    }

    private fun checkStatus() {
        KeyStoreUtil.getAsymmetricKeyAlias()?.let {
            mButtonEncrypt.isEnabled = true
            mButtonDecrypt.isEnabled = true
            mButtonGenerateKey.isEnabled = false
            mEditTextKeyAlias.setText("Key Alias: " + it)
            mEditTextKeyAlias.isEnabled = false
        } ?: run {
            mButtonEncrypt.isEnabled = false
            mButtonDecrypt.isEnabled = false
            mButtonGenerateKey.isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun generateKey() {

        val privateKey = context?.applicationContext?.let {
            KeyStoreUtil.generateAsymmetricKey(
                it,
                "asymmetric_" + mEditTextKeyAlias.text.toString()
            )
        } as KeyStore.PrivateKeyEntry

        val randomKey = CryptoUtil.generateSecureRandom(16)
        val encryptedAesKey = CryptoUtil.encrypt(
            CryptoUtil.CIPHER_RSA,
            randomKey, privateKey.certificate.publicKey
        )
        val result =
            mSharedPreference.edit()
                .putString(SHARED_PREFERENCE_KEY, EncodingUtil.bytesToBase64(encryptedAesKey!!))
                .commit()

        if (result)
            Toast.makeText(context, "Key has saved", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(context, "Key has not saved", Toast.LENGTH_LONG).show()

        checkStatus()

    }

    private fun encrypt() {
        val encryptedKey = EncodingUtil.base64ToBytes(
            mSharedPreference.getString(
                SHARED_PREFERENCE_KEY
                , ""
            )!!
        )
        val asymmetricKey =
            KeyStoreUtil.getKeyEntry(KeyStoreUtil.getAsymmetricKeyAlias()!!) as KeyStore.PrivateKeyEntry
        val aesKey = CryptoUtil.decrypt(CryptoUtil.CIPHER_RSA, encryptedKey, asymmetricKey.privateKey)
        val cipherText = CryptoUtil.encrypt(
            CryptoUtil.CIPHER_AES,
            mEditTextPlainText.text.toString().toByteArray(Charsets.UTF_8),
            SecretKeySpec(aesKey, "AES")
        )

        cipherText?.let {
            mCallBack.setResult(EncodingUtil.bytesToBase64(cipherText))
        } ?: run {
            mCallBack.setResult("NPE")
        }
    }

    private fun decrypt() {

        val encryptedKey = EncodingUtil.base64ToBytes(
            mSharedPreference.getString(
                SHARED_PREFERENCE_KEY
                , ""
            )!!
        )

        val asymmetricKey = KeyStoreUtil.getKeyEntry(KeyStoreUtil.getAsymmetricKeyAlias()!!) as KeyStore.PrivateKeyEntry
        val aesKey = CryptoUtil.decrypt(CryptoUtil.CIPHER_RSA, encryptedKey, asymmetricKey.privateKey)

        val plainText = CryptoUtil.decrypt(
            CryptoUtil.CIPHER_AES,
            EncodingUtil.base64ToBytes(mEditTextCipherText.text.toString()),
            SecretKeySpec(aesKey, "AES")
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