package com.a2k17.dict.spam.spamdict;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by appleowner on 9/30/18.
 */
public class TTSHandler {
    // how many translations should be read out
    private int desiredNumTranslations;

    // text to speech object
    private TextToSpeech textToSpeech;
    private Context context;

    // the current output language
    private Locale currentLanguage;

    // hold on to the most recently returned translations so they can be replayed
    private ArrayList<Translation> currentTranslations;

    // the word that was just looked up in the dictionary
    private String currentWord;

    public TTSHandler(Context activity, int desiredTranslations, Locale language)
    {
        textToSpeech = new TextToSpeech(activity, new TtsListener());
        desiredNumTranslations = desiredTranslations;
        currentLanguage = language;
    }

    /**
     * play a translation object using tts
     * */
    public void playTtsOutput(ArrayList<Translation> translations, String word) {
        int availableTranslations = translations.size();
        textToSpeech.speak(word, TextToSpeech.QUEUE_ADD, null);
        // if the desired number of translations is less than the number of available translations,
        // use that as an upper limit of spoken translations
        int maxTranslations = (availableTranslations < desiredNumTranslations) ? availableTranslations : desiredNumTranslations;
        for (int i = 0; i < maxTranslations; i++)
        {
            String currentTranslation = translations.get(i).getDefinition();
            textToSpeech.speak(currentTranslation, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void setOutputLanguage(Locale language)
    {
        currentLanguage = language;
        textToSpeech.setLanguage(language);
    }

    // replays most recent translation
    // using the global variables currentTranslation and currentWord
    public void replayTranslation()
    {
        // if the current translation, current word and
        // textToSpeech object all are initialized, replay
        if ((currentTranslations != null)
                && (currentWord != null)
                && (textToSpeech != null))
        {
            playTtsOutput(currentTranslations, currentWord);
        }
    }

    // listener to check if tts initialization was implemented correctly
    private class TtsListener implements TextToSpeech.OnInitListener
    {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                if (textToSpeech != null) {
                    int result = textToSpeech.setLanguage(currentLanguage);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "TTS language is not supported", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // TODO: do something if tts initialization failed
                Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setCurrentTranslations(ArrayList<Translation> translations)
    {
        currentTranslations = translations;
    }

    public void setCurrentWord(String word)
    {
        currentWord = word;
    }
}
