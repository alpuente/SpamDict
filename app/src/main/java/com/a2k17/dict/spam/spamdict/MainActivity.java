package com.a2k17.dict.spam.spamdict;

import android.Manifest;
import android.app.DownloadManager;
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
import android.widget.Button;
import android.widget.Toast;

/*import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

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

        //setupNetwork();

        final Button button = (Button) findViewById(R.id.wordbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

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
                System.out.println("Response " + wordToLookUp);
//                lookUpWord(wordToLookUp, requestQueue);
                new CallbackTask().execute(wordToLookUp);
            }
        }
    }

    /**
     * Uses Google spech input dialogs
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

        @Override
        protected String doInBackground(String... params) {
            String word = params[0];

            try {
                String urlString = buildURL(word);
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("app_id", getKey1());
                urlConnection.setRequestProperty("app_key", getKey2());

                // read the output from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                System.out.println("repsonse " + stringBuilder.toString());
                JSONObject myObject = new JSONObject(stringBuilder.toString());
                parseJSON(myObject);
                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            JSONObject jObject = buildJSONObject(result);
//            if (jObject != null)
//            {
//                parseJSON(jObject);
//            }
        }
    }

    public void parseJSON(JSONObject dictEntry) {
        if (dictEntry != null)
        {
            try {
                JSONArray results = (JSONArray) dictEntry.get("results");
                for (int i = 0; i < results.length(); i++)
                {
                    JSONObject result = (JSONObject) results.get(i);
                    JSONArray lexicalEntries = (JSONArray) result.get("lexicalEntries");
                    if (lexicalEntries != null)
                    {
                        for (int j = 0; j < lexicalEntries.length(); j++)
                        {
                            JSONObject lexicalEntry = (JSONObject) lexicalEntries.get(j);
                            JSONArray entries = (JSONArray) lexicalEntry.get("entries");
                            for (int k = 0; k < entries.length(); k++)
                            {
                                JSONObject entry = (JSONObject) entries.get(k);
                                JSONArray senses = (JSONArray) entry.get("senses");
                                for (int l = 0; l < senses.length(); l++)
                                {
                                    JSONObject sense = (JSONObject) senses.get(l);
                                    JSONArray translations = (JSONArray) sense.get("translations");
                                    for (int m = 0; m < translations.length(); m++)
                                    {
                                        JSONObject translation = (JSONObject) translations.get(m);
                                        String translationText = translation.getString("text");
                                        System.out.println(translationText);
                                    }
                                }
                            }
                            System.out.println(lexicalEntry.toString());
                        }
                    }
//                    result.get()
                }
            } catch (JSONException e) {
                // TODO: no results
                e.printStackTrace();
            }
        }
    }
}
