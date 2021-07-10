package me.robertlit.runcode.data;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class Language {

    @SerializedName("language")
    private final String name;
    private final Set<String> aliases;

    public Language(String name, Set<String> aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAliases() {
        return aliases;
    }
}
