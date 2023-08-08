package com.muradakhundov.pockettranslator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.muradakhundov.pockettranslator.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    var fromLanguages = arrayOf("From", "English", "Russian", "Turkish", "Czech")
    var toLanguages = arrayOf("To", "English", "Turkish", "Czech", "Russian")

    companion object {
        val REQUEST_PERMISSION_CODE = 1
        var languageCode = 0
        var fromLanguageCode = 0
        var toLanguageCode = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)


        binding.idFromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fromLanguageCode = getLanguageCode(fromLanguages[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }

        var fromAdapter = ArrayAdapter(this, R.layout.spinner_item, fromLanguages)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.idFromSpinner.adapter = fromAdapter


        binding.idToSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                toLanguageCode = getLanguageCode(toLanguages[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        var toAdapter = ArrayAdapter(this, R.layout.spinner_item, toLanguages)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.idToSpinner.adapter = toAdapter


        binding.translateBtn.setOnClickListener {
            if (binding.idEdtSource.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter your text to translate", Toast.LENGTH_LONG)
            } else if (fromLanguageCode == 0) {
                Toast.makeText(this, "please select source language", Toast.LENGTH_SHORT)
            } else if (toLanguageCode == 0) {
                Toast.makeText(
                    this,
                    "please select the language to make translation",
                    Toast.LENGTH_SHORT
                )
            } else {
                translateText(fromLanguageCode, toLanguageCode, binding.idEdtSource.text.toString())
            }
        }

        binding.idTVmic.setOnClickListener {
            var i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Convert into Text")
            try {
                startActivityForResult(i, REQUEST_PERMISSION_CODE)
            } catch (e: Exception) {
                Toast.makeText(this, "error ${e.message.toString()}", Toast.LENGTH_SHORT)

            }
        }


        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                var result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                binding.idEdtSource.setText(result?.get(0) ?: "Something went wrong")

            }
        }
    }

    fun translateText(fromLanguageCode: Int, toLanguageCode: Int, source: String) {
        binding.translatedTV.setText("Translating")
        var options = FirebaseTranslatorOptions
            .Builder()
            .setSourceLanguage(fromLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()



        var translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        var conditions = FirebaseModelDownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(object : OnSuccessListener<Void>{
            override fun onSuccess(p0: Void?) {
                binding.translatedTV.setText("Translating")
                translator.translate(source).addOnSuccessListener(object : OnSuccessListener<String>{
                    override fun onSuccess(p0: String?) {
                        binding.translatedTV.setText(p0)
                    }

                }).addOnFailureListener {
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT)

                }
            }

        }).addOnFailureListener {
            Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT)

        }
    }

    fun getLanguageCode(language: String): Int {
        var languageCode = 0
        when (language) {
            "English" -> languageCode = FirebaseTranslateLanguage.EN
            "Turkish" -> languageCode = FirebaseTranslateLanguage.TR
            "Czech" -> languageCode = FirebaseTranslateLanguage.CS
            "Russian" -> languageCode = FirebaseTranslateLanguage.RU
            else -> 0
        }
        return languageCode
    }
}