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
package net.daboross.bungeedev.ncommon.commands;

import net.daboross.bungeedev.mysqlmap.api.MapTable;
import net.daboross.bungeedev.mysqlmap.api.ResultRunnable;
import net.daboross.bungeedev.ncommon.ColorList;
import net.daboross.bungeedev.ncommon.NCommonPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class SQLMapTestCommand extends Command {

    private final MapTable<String, String> table;

    public SQLMapTestCommand(NCommonPlugin plugin) {
        super("sqltest");
        this.table = plugin.getDatabase().getStringToStringTable("sql_test");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("ncommon.sqltest")) {
            sender.sendMessage(ColorList.ERR + "You do not have permission to execute this command.");
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(ColorList.ERR + "Usage: " + ColorList.ERR_ARGS + "/sqltest <set|get> <key> [value]");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(ColorList.ERR + "Usage: " + ColorList.ERR_ARGS + "/sqltest set <key> <value>");
                    return;
                }
                table.set(args[1], args[2], new ResultRunnable<Boolean>() {
                    @Override
                    public void runWithResult(Boolean value) {
                        if (value) {
                            sender.sendMessage(ColorList.REG + "Set " + ColorList.DATA + args[1] + ColorList.REG + " to " + ColorList.DATA + args[2] + ColorList.REG + ".");
                        } else {
                            sender.sendMessage(ColorList.ERR + "Failed to set " + ColorList.ERR_ARGS + args[1] + ColorList.ERR + " to " + ColorList.ERR_ARGS + args[2] + ColorList.ERR + ".");
                        }
                    }
                });
                break;
            case "get":
                if (args.length < 2) {
                    sender.sendMessage(ColorList.ERR + "Usage: " + ColorList.ERR_ARGS + "/sqltest get <key>");
                    return;
                }
                table.get(args[1], new ResultRunnable<String>() {
                    @Override
                    public void runWithResult(String value) {
                        sender.sendMessage(ColorList.DATA + args[1] + ColorList.REG + " is set to " + ColorList.DATA + value);
                    }
                });
                break;
            default:
                sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/sqltest <set|get>");
        }
    }
}
