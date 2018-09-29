package com.a2k17.dict.spam.spamdict;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by appleowner on 9/27/18.
 */

public class JSONResponseParser {

    public static void parseJSON(JSONObject dictEntry, ArrayList<Translation> translations) {
        if (dictEntry != null)
        {
            try {
                if (dictEntry.has("results"))
                {
                    JSONArray results = (JSONArray) dictEntry.get("results");
                    // for each result, parse each lexical entry within it
                    for (int i = 0; i < results.length(); i++)
                    {
                        JSONObject result = (JSONObject) results.get(i);
                        // if there are lexical entries, parse them all
                        if (result.has("lexicalEntries"))
                        {
                            JSONArray lexicalEntries = (JSONArray) result.get("lexicalEntries");
                            parseLexicalEntries(lexicalEntries, translations);
                        }
                    }
                }
            } catch (JSONException e) {
                // TODO: no results
                e.printStackTrace();
            }
        }
    }

    /*
       parse each lexical entry within a result returned by HTTP GET request to Oxford Dictionaries
            lexicalEntry {
                derivativeOf (ArrayOfRelatedEntries, optional): Other words from which this one derives ,
                derivatives (ArrayOfRelatedEntries, optional): Other words from which their Sense derives ,
                entries (Array[Entry], optional),
                grammaticalFeatures (GrammaticalFeaturesList, optional),
                language (string): IANA language code ,
                lexicalCategory (string): A linguistic category of words (or more precisely lexical items), generally defined by the syntactic or morphological behaviour of the lexical item in question, such as noun or verb ,
                notes (CategorizedTextList, optional),
                pronunciations (PronunciationsList, optional),
                text (string): A given written or spoken realisation of a an entry. ,
                variantForms (VariantFormsList, optional): Various words that are used interchangeably depending on the context, e.g 'a' and 'an'
            }

        https://developer.oxforddictionaries.com/documentation#!/Translation/get_entries_source_translation_language_word_id_translations_target_translation_language
     */
    private static void parseLexicalEntries(JSONArray lexicalEntries, ArrayList<Translation> translations)
    {
        try {

            for (int j = 0; j < lexicalEntries.length(); j++)
            {
                JSONObject lexicalEntry = (JSONObject) lexicalEntries.get(j);
                // if there are entries, parse them all
                if (lexicalEntry.has("entries"))
                {
                    JSONArray entries = (JSONArray) lexicalEntry.get("entries");
                    parseEntries(entries, translations);
                }
            }
        }
        catch (JSONException e)
        {
            // TODO: better error handling
            e.printStackTrace();
        }
    }


    /*
       Entry {
            etymologies (arrayofstrings, optional): The origin of the word and the way in which its meaning has changed throughout history ,
            grammaticalFeatures (GrammaticalFeaturesList, optional),
            homographNumber (string, optional): Identifies the homograph grouping. The last two digits identify different entries of the same homograph. The first one/two digits identify the homograph number. ,
            notes (CategorizedTextList, optional),
            pronunciations (PronunciationsList, optional),
            senses (Array[Sense], optional): Complete list of senses ,
            variantForms (VariantFormsList, optional): Various words that are used interchangeably depending on the context, e.g 'a' and 'an'
       }

       https://developer.oxforddictionaries.com/documentation#!/Translation/get_entries_source_translation_language_word_id_translations_target_translation_language
     */
    private static void parseEntries(JSONArray entries, ArrayList<Translation> translations)
    {
        try {
            for (int k = 0; k < entries.length(); k++)
            {
                JSONObject entry = (JSONObject) entries.get(k);
                // parse each sense object if there are senses
                if (entry.has("senses"))
                {
                    JSONArray senses = (JSONArray) entry.get("senses");
                    parseSenses(senses, translations);
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private static void parseSenses(JSONArray senses, ArrayList<Translation> translations) {
        try
        {
            // parse each sense object if there are senses
            for (int l = 0; l < senses.length(); l++)
            {
                JSONObject sense = (JSONObject) senses.get(l);
                parseSense(sense, translations);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /*

    Sense {
        crossReferenceMarkers (arrayofstrings, optional): A grouping of crossreference notes. ,
        crossReferences (CrossReferencesList, optional),
        definitions (arrayofstrings, optional): A list of statements of the exact meaning of a word ,
        domains (arrayofstrings, optional): A subject, discipline, or branch of knowledge particular to the Sense ,
        examples (ExamplesList, optional),
        id (string, optional): The id of the sense that is required for the delete procedure ,
        notes (CategorizedTextList, optional),
        pronunciations (PronunciationsList, optional),
        regions (arrayofstrings, optional): A particular area in which the Sense occurs, e.g. 'Great Britain' ,
        registers (arrayofstrings, optional): A level of language usage, typically with respect to formality. e.g. 'offensive', 'informal' ,
        short_definitions (arrayofstrings, optional): A list of short statements of the exact meaning of a word ,
        subsenses (Array[Sense], optional): Ordered list of subsenses of a sense ,
        thesaurusLinks (Array[thesaurusLink], optional): Ordered list of links to the Thesaurus Dictionary ,
        translations (TranslationsList, optional),
        variantForms (VariantFormsList, optional): Various words that are used interchangeably depending on the context, e.g 'duck' and 'duck boat'
      }
        https://developer.oxforddictionaries.com/documentation#!/Translation/get_entries_source_translation_language_word_id_translations_target_translation_language
     */
    private static void parseSense(JSONObject sense, ArrayList<Translation> translationObjects)
    {
        try {
            if (sense.has("translations"))
            {
                JSONArray translations = (JSONArray) sense.get("translations");
                for (int m = 0; m < translations.length(); m++) {
                    JSONObject translation = (JSONObject) translations.get(m);
                    // add the translation to the list of translations
                    // TODO: deal with duplicates
                    translationObjects.add(parseTranslation(translation));
                }
            }
            else {
                // if there are no translations, check if there are subsenses and parse those instead
                if (sense.has("subsenses"))
                {
                    JSONArray subSenses = (JSONArray) sense.get("subsenses");
                    if (subSenses != null)
                    {
                        parseSenses(subSenses, translationObjects);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO: better error handling
            e.printStackTrace();
        }
    }

    /*

        Inline Model 8 {
            domains (arrayofstrings, optional): A subject, discipline, or branch of knowledge particular to the translation ,
            grammaticalFeatures (GrammaticalFeaturesList, optional),
            language (string): IANA language code specifying the language of the translation ,
            notes (CategorizedTextList, optional),
            regions (arrayofstrings, optional): A particular area in which the translation occurs, e.g. 'Great Britain' ,
            registers (arrayofstrings, optional): A level of language usage, typically with respect to formality. e.g. 'offensive', 'informal' ,
            text (string)
        }

        https://developer.oxforddictionaries.com/documentation#!/Translation/get_entries_source_translation_language_word_id_translations_target_translation_language
     */
    private static Translation parseTranslation(JSONObject translation) throws JSONException
    {
        String translationText = translation.getString("text");
        System.out.println( "here is the translation text: " + translationText);
        Translation translationObject = new Translation(translationText);
        return translationObject;
    }
}
