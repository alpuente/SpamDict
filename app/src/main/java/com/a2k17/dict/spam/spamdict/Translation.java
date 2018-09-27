package com.a2k17.dict.spam.spamdict;

/**
 * Created by appleowner on 9/27/18.
 * Class to hold translation and example
 */

public class Translation {
    private String definition;
    private String example;

    public Translation(String definition)
    {
        this.definition = definition;
    }

    public Translation(String definition, String example)
    {
        this.definition = definition;
        this.example = example;
    }

    public boolean hasExample()
    {
        return (example == null);
    }

    public String getDefinition(){
        return definition;
    }

    public String getExample()
    {
        return example;
    }
 }
