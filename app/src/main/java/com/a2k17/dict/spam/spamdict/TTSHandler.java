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
    private int desiredNumTranslations;
    private TextToSpeech textToSpeech;
    private Context context;

    public TTSHandler(Context activity, int desiredTranslations)
    {
        textToSpeech = new TextToSpeech(activity, new TtsListener());
        desiredNumTranslations = desiredTranslations;
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
                        Toast.makeText(context, "TTS language is not supported", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // TODO: do something if tts initialization failed
                Toast.makeText(context, "TTS initialization failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
