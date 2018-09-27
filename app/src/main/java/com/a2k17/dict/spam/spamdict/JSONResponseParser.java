package com.a2k17.dict.spam.spamdict;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by appleowner on 9/27/18.
 */

public class JSONResponseParser {

    public static void parseJSON(JSONObject dictEntry) {
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
                                        System.out.println( "here is the translation text: " + translationText);
                                    }
                                }
                            }
                            System.out.println(lexicalEntry.toString());
                        }
                    }
                }
            } catch (JSONException e) {
                // TODO: no results
                e.printStackTrace();
            }
        }
    }

    private void parseSearchResults()
    {

    }
}
