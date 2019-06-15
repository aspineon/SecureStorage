package com.ehsanmashhadi.securestorage

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), CallBackInterface {

    private lateinit var mRadioGroupMethod: RadioGroup
    private lateinit var mRadioButtonAsymmetric: RadioButton
    private lateinit var mRadioButtonSymmetric: RadioButton
    private lateinit var mRadioButtonPbkdf: RadioButton
    private lateinit var mFragmentContainer: FrameLayout
    private lateinit var mTextViewResult: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUiComponents()
        initListeners()
        mRadioButtonAsymmetric.isChecked = true
    }

    private fun initUiComponents() {

        mRadioGroupMethod = findViewById(R.id.radiogroup_mainactivity_method)
        mRadioButtonAsymmetric = findViewById(R.id.radiobutton_mainactivity_asymmetric)
        mRadioButtonSymmetric = findViewById(R.id.radiobutton_mainactivity_symmetric)
        mRadioButtonPbkdf = findViewById(R.id.radiobutton_mainactivity_pbkdf)
        mTextViewResult = findViewById(R.id.textview_mainactivity_result)
        mFragmentContainer = findViewById(R.id.framelayout_mainactivity_container)
    }

    private fun initListeners() {

        mRadioGroupMethod.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.radiobutton_mainactivity_asymmetric -> {
                    if (!KeyStoreUtil.keyStoreAsymmetricAvailable()) {
                        FragmentUtil.replaceFragmentWithFragment(
                            supportFragmentManager,
                            NotSupportFragment(),
                            R.id.framelayout_mainactivity_container
                        )
                    } else {
                        FragmentUtil.replaceFragmentWithFragment(
                            supportFragmentManager,
                            AsymmetricFragment(),
                            R.id.framelayout_mainactivity_container
                        )
                    }
                }

                R.id.radiobutton_mainactivity_symmetric -> {
                    if (!KeyStoreUtil.keyStoreSymmetricAvailable()) {
                        FragmentUtil.replaceFragmentWithFragment(
                            supportFragmentManager,
                            NotSupportFragment(),
                            R.id.framelayout_mainactivity_container
                        )
                    } else {
                        FragmentUtil.replaceFragmentWithFragment(
                            supportFragmentManager,
                            SymmetricFragment(),
                            R.id.framelayout_mainactivity_container
                        )
                    }
                }

                R.id.radiobutton_mainactivity_pbkdf -> {
                    FragmentUtil.replaceFragmentWithFragment(
                        supportFragmentManager,
                        KeyDerivationFragment(),
                        R.id.framelayout_mainactivity_container
                    )
                }
            }
        }
    }

    override fun setResult(result: String) {
        mTextViewResult.text = "Result: " + result
    }
}