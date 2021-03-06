/*
 * Copyright (C) 2013-2014 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.bungeedev.ncommon.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class AliasConfig implements Listener {

    public final File configFile;
    private final Map<String, CommandAlias> aliases = new HashMap<>();
    private final Map<String, Map<String, CommandAlias>> perServer = new HashMap<>();
    private final Plugin plugin;

    public AliasConfig(Plugin p) throws IOException {
        plugin = p;
        configFile = new File(p.getDataFolder(), "aliases.txt");
        if (!configFile.exists()) {
            configFile.createNewFile();
        } else {
            loadConfig();
        }
    }

    private void loadConfig() throws IOException {
        loadAliasFile(configFile, aliases);
        plugin.getLogger().log(Level.INFO, "Loaded aliases. (" + aliases + ")");
        for (String serverName : plugin.getProxy().getServers().keySet()) {
            File aliasFile = new File(plugin.getDataFolder(), "aliases-" + serverName + ".txt");
            Map<String, CommandAlias> aliasMap = new HashMap<>();
            perServer.put(serverName, aliasMap);
            if (aliasFile.exists()) {
                loadAliasFile(aliasFile, aliasMap);
                plugin.getLogger().log(Level.INFO, "Loaded aliases for server " + serverName + ". (" + aliasMap + ")");
            }
        }
    }

    private void loadAliasFile(File file, Map<String, CommandAlias> into) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)) {
                try (BufferedReader bf = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = bf.readLine()) != null) {
                        for (String l : line.split(",")) {
                            String[] aliasAndResult = l.split("#", 2);
                            if (aliasAndResult.length < 2) {
                                continue;
                            }
                            into.put(aliasAndResult[0].split(" ", 2)[0].trim().toLowerCase(), new CommandAlias(aliasAndResult[0], aliasAndResult[1]));
                        }
                    }
                }
            }
        }
    }

    public void reloadConfig() {
        aliases.clear();
        perServer.clear();
        try {
            loadConfig();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "[AliasConfig] Failed to reload alias config", ex);
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!e.getMessage().startsWith("/") || !(e.getSender() instanceof ProxiedPlayer)) return;
        String preparedMessage = e.getMessage().split(" ", 2)[0].trim().substring(1).toLowerCase();
        CommandAlias alias = aliases.get(preparedMessage);
        if (alias != null) {
            e.setMessage(alias.getFullReplacement(e.getMessage()));
        } else {
            alias = perServer.get(((ProxiedPlayer) e.getSender()).getServer().getInfo().getName()).get(preparedMessage);
            if (alias != null) {
                e.setMessage(alias.getFullReplacement(e.getMessage()));
            }
        }
    }
}
