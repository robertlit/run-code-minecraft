package me.robertlit.runcode.command;

import me.robertlit.runcode.CodeExecutor;
import me.robertlit.runcode.CodeSession;
import me.robertlit.runcode.settings.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CodeCommand implements TabExecutor {

    private final Plugin plugin;
    private final CodeExecutor executor;

    public CodeCommand(Plugin plugin, CodeExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player && sender.hasPermission("runcode.use"))) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_GREEN + "/code <language>");
            return false;
        }
        if (!executor.getLanguages().contains(args[0])) {
            sender.sendMessage(Lang.languageNotFound);
            return true;
        }
        Player player = (Player) sender;
        new CodeSession(plugin, executor, player, args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return StringUtil.copyPartialMatches(args[0], executor.getLanguages(), new ArrayList<>());
    }
}
