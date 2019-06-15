package com.ehsanmashhadi.securestorage

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

class SymmetricFragment : Fragment() {

    private lateinit var mViewRoot: View
    private lateinit var mEditTextKeyAlias: EditText
    private lateinit var mEditTextPlainText: EditText
    private lateinit var mEditTextCipherText: EditText
    private lateinit var mButtonGenerateKey: Button
    private lateinit var mButtonEncrypt: Button
    private lateinit var mButtonDecrypt: Button
    private lateinit var mCallBack: CallBackInterface


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewRoot = inflater.inflate(R.layout.fragment_symmetric, null)
        return mViewRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiComponents()
        initListeners()
    }

    private fun initListeners() {
        if (Build.VERSION.SDK_INT >= 23) {
            mButtonGenerateKey.setOnClickListener {
                generateKey()
                checkStatus()
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
        KeyStoreUtil.getSymmetricKeyAlias()?.let {
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        KeyStoreUtil.generateSymmetricKey("symmetric_" + mEditTextKeyAlias.text.toString())
    }

    private fun encrypt() {

        val cipherText = CryptoUtil.encrypt(
            CryptoUtil.CIPHER_AES,
            mEditTextPlainText.text.toString().toByteArray(Charsets.UTF_8),
            KeyStoreUtil.getKey(KeyStoreUtil.getSymmetricKeyAlias()!!)!!
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
            (KeyStoreUtil.getKey(KeyStoreUtil.getSymmetricKeyAlias()!!)!!)
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