package com.a2k17.dict.spam.spamdict;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;


import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    int PERMISSION_MULTIPLE;

    // Speech input
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private int desiredNumberOfTranslations = 3;

    // handler for text to speech functionality
    private TTSHandler ttsHandler;
    private String inputLanguage;
    private Locale outputLanguage;

    // input string to use in the http request
    private String inputLanguageURL = "en";
    // input string to use in the http request
    private String outputLanguageURL = "es";

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

        // Set defaults
        outputLanguage = Locale.US;
        inputLanguage = "es-mx";
        inputLanguageURL = "es";
        outputLanguageURL = "en";
        // initialize TTS
        ttsHandler = new TTSHandler(this, desiredNumberOfTranslations, outputLanguage);
        //setupNetwork();

        final Button button = (Button) findViewById(R.id.wordbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // prompt speech input when button is pressed
                promptSpeechInput();
            }
        });

        final ImageButton replayButton = (ImageButton) findViewById(R.id.replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // prompt speech input when button is pressed
                ttsHandler.replayTranslation();
            }
        });

        final Switch languageSwitch = (Switch) findViewById(R.id.languageSwitch);
        languageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // english to spanish
                    inputLanguage = "en";
                    inputLanguageURL = "en";
                    outputLanguageURL = "es";
                    // new locale for spanish
                    Locale spanish = new Locale("es", "ES");
                    ttsHandler.setOutputLanguage(spanish);
                } else {
                    // The toggle is disabled
                    // spanish to english
                    inputLanguage = "es-mx";
                    inputLanguageURL = "es";
                    outputLanguageURL = "en";
                    ttsHandler.setOutputLanguage(Locale.US);
                }
            }
        });

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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, inputLanguage);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String buildURL(String word) {
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required
        System.out.println("https://od-api.oxforddictionaries.com:443/api/v1/entries/" + inputLanguageURL +
                "/" + word_id + "/translations=" + outputLanguageURL);
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + inputLanguageURL +
                "/" + word_id + "/translations=" + outputLanguageURL;
    }


    //in android calling network requests on the main thread forbidden by default
    //create class to do async job
    private class CallbackTask extends AsyncTask<String, Integer, String> {
        // the translations returned from Oxford dictionary
        ArrayList<Translation> translations;
        // the word spoken by user
        String word;

        // in background, query dictionary and parse response
        @Override
        protected String doInBackground(String... params) {
            // the word spoken by user
            word = params[0];

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

                // set the current translation info in ttsHandler
                ttsHandler.setCurrentTranslations(translations);
                ttsHandler.setCurrentWord(word);
                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }

        }

        // read out the translations
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ttsHandler.playTtsOutput(translations, word);
        }
    }
}
