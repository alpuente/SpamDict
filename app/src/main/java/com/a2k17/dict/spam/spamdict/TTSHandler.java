package com.a2k17.dict.spam.spamdict;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by appleowner on 9/30/18.
 */
public class TTSHandler {
    // how many translations should be read out
    private int desiredNumTranslations;

    // text to speech object
    private TextToSpeech outputTextToSpeech;
    private TextToSpeech inputTextToSpeech;
    private Context context;

    // the current output language
//    private Locale currentLanguage;


    // hold on to the most recently returned translations so they can be replayed
    private ArrayList<Translation> currentTranslations;

    // the word that was just looked up in the dictionary
    private String currentWord;

    public TTSHandler(Context activity, int desiredTranslations, Locale inputLanguage, Locale outputLanguage)
    {
        // set to read output language
        // ex. read out translation of hola in english
        outputTextToSpeech = new TextToSpeech(activity, new TtsListener());
        outputTextToSpeech.setLanguage(outputLanguage);

        // set to read the input language
        // ex. read out hola in spanish before reading translation in english
        inputTextToSpeech = new TextToSpeech(activity, new TtsListener());
        inputTextToSpeech.setLanguage(inputLanguage);

        desiredNumTranslations = desiredTranslations;
    }

    /**
     * play a translation object using tts
     * */
    public void playTtsOutput(ArrayList<Translation> translations, String word) {
        int availableTranslations = translations.size();
        // read out the input word that was searched in the input language
        inputTextToSpeech.speak(word, TextToSpeech.QUEUE_ADD, null);

        // if the desired number of translations is less than the number of available translations,
        // use that as an upper limit of spoken translations
        int maxTranslations = (availableTranslations < desiredNumTranslations) ? availableTranslations : desiredNumTranslations;
        for (int i = 0; i < maxTranslations; i++)
        {
            String currentTranslation = translations.get(i).getDefinition();
            outputTextToSpeech.speak(currentTranslation, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void setLanguages(Locale inputLanguage, Locale outputLanguage)
    {
        outputTextToSpeech.setLanguage(outputLanguage);
        inputTextToSpeech.setLanguage(inputLanguage);
    }

    // replays most recent translation
    // using the global variables currentTranslation and currentWord
    public void replayTranslation()
    {
        // if the current translation, current word and
        // textToSpeech object all are initialized, replay
        if ((currentTranslations != null)
                && (currentWord != null)
                && (outputTextToSpeech != null))
        {
            playTtsOutput(currentTranslations, currentWord);
        }
    }

    // listener to check if tts initialization was implemented correctly
    private class TtsListener implements TextToSpeech.OnInitListener
    {
        @Override
        public void onInit(int status) {
            //TODO: handle all this better
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "TTS language is not supported", Toast.LENGTH_LONG).show();
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
