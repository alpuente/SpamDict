package com.a2k17.dict.spam.spamdict;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;


import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    int PERMISSION_AUDIO;
    int PERMISSION_MULTIPLE;

    // Speech input
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private int CHECK_TTS_CODE;
    private int DESIRED_NUMBER_OF_TRANSLATIONS = 3;
    private TextToSpeech textToSpeech;

    static {
        System.loadLibrary("keys");
    }

    public native String getKey1();
    public native String getKey2();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck;
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET}, PERMISSION_MULTIPLE);
        }

        // initialize TTS
        textToSpeech = new TextToSpeech(this, new TtsListener());
        //setupNetwork();

        final Button button = (Button) findViewById(R.id.wordbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // prompt speech input when button is pressed
                promptSpeechInput();
            }
        });

    }

    /**
     *  Called after speech button is pressed
     * @param requestCode Which activity has completed
     * @param resultCode Result of the completed activity
     * @param data The intent from the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
        if (requestCode == REQ_CODE_SPEECH_INPUT)
        {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String wordToLookUp = result.get(0);
//                lookUpWord(wordToLookUp, requestQueue);
                new CallbackTask().execute(wordToLookUp);
            }
        }
    }

    /**
     * Uses Google speech input dialogs
     * */
    public void promptSpeechInput() {
        // Get Google speech input
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-mx");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String buildURL(String word) {
        final String language = "es";
        final String target_lang = "en";
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id + "/translations=" + target_lang;
    }


    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, String> {
        // the translations returned from Oxford dictionary
        ArrayList<Translation> translations;

        @Override
        protected String doInBackground(String... params) {
            // the word spoken by user
            String word = params[0];

            try {
                String urlString = buildURL(word);
                URL url = new URL(urlString);

                // TODO: should I only be creating this connection once?
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                // get application keys to connect to Oxford dictionaries
                urlConnection.setRequestProperty("app_id", getKey1());
                urlConnection.setRequestProperty("app_key", getKey2());

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                JSONObject myObject = new JSONObject(stringBuilder.toString());
                translations = new ArrayList<>();
                JSONResponseParser.parseJSON(myObject, translations);
                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            playTtsOutput(translations);
        }
    }

    // listener to check if tts initialization was implemented correctly
    private class TtsListener implements TextToSpeech.OnInitListener
    {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                if (textToSpeech != null) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        //Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // TODO: do something if tts initialization failed
                //Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * play a translation object using tts
     * */
    public void playTtsOutput(ArrayList<Translation> translations) {
        int availableTranslations = translations.size();
        // if the desired number of translations is less than the number of available translations,
        // use that as an upper limit of spoken translations
        int maxTranslations = (availableTranslations < DESIRED_NUMBER_OF_TRANSLATIONS) ? availableTranslations : DESIRED_NUMBER_OF_TRANSLATIONS;
        for (int i = 0; i < maxTranslations; i++)
        {
            String currentTranslation = translations.get(i).getDefinition();
            textToSpeech.speak(currentTranslation, TextToSpeech.QUEUE_ADD, null);
        }
    }
}
