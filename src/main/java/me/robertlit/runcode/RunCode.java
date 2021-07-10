package me.robertlit.runcode;

import me.robertlit.runcode.command.CodeCommand;
import me.robertlit.runcode.settings.Lang;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class RunCode extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLang();
        CodeExecutor executor = new CodeExecutor(getConfig().getBoolean("aliases"));
        try {
            executor.loadLanguages();
        } catch (IOException e) {
            getLogger().warning("Unable to load languages, disabling");
            getServer().getPluginManager().disablePlugin(this);
        }
        getCommand("code").setExecutor(new CodeCommand(this, executor));
    }

    private void loadLang() {
        ConfigurationSection lang = getConfig().getConfigurationSection("lang");
        Lang.languageNotFound = color(lang.getString("language-not-found"));
        Lang.executing = color(lang.getString("executing"));
        Lang.executeFail = color(lang.getString("execute-fail"));
        Lang.executeSuccess = color(lang.getString("execute-success"));
        Lang.quitting = color(lang.getString("quitting"));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
