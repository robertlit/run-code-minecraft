package me.robertlit.runcode;

import me.robertlit.runcode.settings.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CodeSession {

    private static final String EXECUTE = "execute";
    private static final String TERMINATE = "quit";

    private static final Map<String, String> replacements = new HashMap<>();

    static {
        replacements.put("....", "    ");
    }

    private static String replace(String line) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            line = line.replace(replacement.getKey(), replacement.getValue());
        }
        return line;
    }

    private final CodeExecutor executor;
    private final UUID user;
    private final String language;
    private final List<String> source = new ArrayList<>();
    private final Conversation conversation;
    private boolean terminated = false;

    public CodeSession(Plugin plugin, CodeExecutor executor, Player user, String language) {
        this.user = user.getUniqueId();
        this.executor = executor;
        this.language = language;

        this.conversation = new ConversationFactory(plugin)
                .withFirstPrompt(new CodePrompt())
                .withLocalEcho(false)
                .withPrefix(context -> ChatColor.DARK_GRAY + "> " + ChatColor.AQUA)
                .withTimeout(120)
                .buildConversation(user);
        conversation.begin();
    }

    public void handle(@NotNull String line) {
        if (line.equalsIgnoreCase(EXECUTE)) {
            runOnPlayer(player -> player.sendMessage(execute() ? Lang.executing
                    : Lang.executeFail));
        } else if (line.equalsIgnoreCase(TERMINATE)) {
            terminate();
            runOnPlayer(player -> player.sendMessage(Lang.quitting));
        }
        else {
            source.add(replace(line));
        }
    }

    public boolean execute() {
        terminate();
        if (source.isEmpty()) {
            return false;
        }
        executor.execute(language, String.join("\n", source)).thenAccept(response -> runOnPlayer(player -> {
            if (!response.success()) {
                player.sendMessage(Lang.executeFail);
                return;
            }
            player.sendMessage(Lang.executeSuccess
                    .replace("%lang%", response.getLanguage())
                    .replace("%version%", response.getVersion())
                    .replace("%out%", (response.getCompile() != null ? response.getCompile().getOutput() + "\n" : "") +
                            (response.getRun() != null ? response.getRun().getOutput() : "")));
        }));
        return true;
    }

    public void terminate() {
        conversation.abandon();
        terminated = true;
    }

    private void runOnPlayer(Consumer<Player> consumer) {
        Player player = Bukkit.getPlayer(user);
        if (player != null) {
            consumer.accept(player);
        }
    }

    private class CodePrompt extends StringPrompt {

        @NotNull
        @Override
        public String getPromptText(@NotNull ConversationContext context) {
            if (source.isEmpty()) {
                return "";
            }
            return source.get(source.size() - 1);
        }

        @Nullable
        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
            handle(input != null ? input : "\n");
            return terminated ? null : this;
        }
    }
}
