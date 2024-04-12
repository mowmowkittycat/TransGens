package mc.warp.transmitgenerators.commands

import com.idlegens.idlecore.gui.InventoryManager.openInventory
import com.idlegens.idlecore.stats.StatGUI
import de.tr7zw.nbtapi.NBTBlock
import mc.warp.transmitgenerators.Generator
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.guis.GenList
import mc.warp.transmitgenerators.guis.GenUpgradeGUI
import mc.warp.transmitgenerators.type.WarpPlayer
import mc.warp.transmitgenerators.utils.Format.sendText
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
import mc.warp.transmitgenerators.utils.Messages.playSound
import mc.warp.transmitgenerators.utils.scheduler.schedule
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*


class GenCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {


        var player = sender

        if (player is Player) {
            if (!player.hasPermission("transmitgens.command")) {
                sendText(player, getLangMessage("command.error.permission"))
                playSound(player, "command.error")
                return false
            }
        }


        when (args.getOrNull(0)?.lowercase()) {
            "give" -> {
                if (!player.hasPermission("transmitgens.command.generator.get")) {
                    sendText(player, getLangMessage("command.error.permission"))
                    playSound(player, "command.error")
                    return false
                }
                if (args.getOrNull(1) == null) {
                    sendText(player, getLangMessage("command.error.gen.null"))
                    playSound(player, "command.error")
                    return false
                }
                var gen = getDataStore().getGenerator(args.get(1));
                if (gen == null) {
                    sendText(player, getLangMessage("command.error.gen.existence", args.get(1)))
                    playSound(player, "command.error")
                    return false
                }
                if (args.getOrNull(2) != null) {
                    var sendPlayer = Bukkit.getPlayer(args.get(2))
                    if (sendPlayer != null) {
                        sendPlayer.inventory.addItem(gen.getBlock())
                        playSound(player, "command.success")
                    } else {
                        sendText(player, getLangMessage("command.error.player.existence", args.get(2)))
                        playSound(player, "command.error")
                        return false
                    }
                } else if ( player is Player ){
                    player.inventory.addItem(gen.getBlock())
                    playSound(player, "command.success")
                }

            }
            "slot", "slots" -> {
                if (!player.hasPermission("transmitgens.command.slots")) {
                    sendText(player, getLangMessage("command.error.permission"))
                    playSound(player, "command.error")
                    return false
                }
                if (args.getOrNull(1) == null) {
                    sendText(player, getLangMessage("command.error.usage", "/transmitgens slot (add/remove/amount)"))
                    playSound(player, "command.error")
                    return false
                }

                if (args[1].equals("set", ignoreCase = true)) {
                    if (args.getOrNull(2) == null) {
                        sendText(player, getLangMessage("command.error.usage", "/transmitgens slot set <number> <player>"))
                        playSound(player, "command.error")
                        return false
                    }
                    var num = Integer.parseInt(args.getOrNull(2))

                    var data: WarpPlayer;
                    var save = false;

                    if (player is Player && args.getOrNull(3) == null) data = getDataStore().getPlayer(player)!!
                    else if (args.getOrNull(3) != null) {

                        var tempPlayer = Bukkit.getOfflinePlayer(args[3])
                        if (tempPlayer == null) {
                            sendText(player, getLangMessage("command.error.player.existence", args[3]))
                            playSound(player, "command.error")
                            return false
                        }
                        if (!tempPlayer.isOnline) {
                            data = getDataStore().loadPlayer(tempPlayer)!!
                            save = true
                        } else {
                            data = getDataStore().getPlayer(tempPlayer)!!
                        }

                    } else {
                        sendText(player, getLangMessage("command.error.usage", "/transmitgens slot set <number> <player>"))
                        playSound(player, "command.error")
                        return false
                    }

                    data.maxGenSlots = num
                    if (!save) getDataStore().setPlayer(Bukkit.getPlayer(data.UUID)!!, data)
                    else getDataStore().saveWarpPlayer(data);

                    return true
                }

                if (args[1].equals("add", ignoreCase = true) || args[1].equals("remove", ignoreCase = true)) {

                    if (args.getOrNull(2) == null) {
                        sendText(player, getLangMessage("command.error.usage", "/transmitgens slot (add/remove) <number> <player>"))
                        playSound(player, "command.error")
                        return false
                    }
                    var num = Integer.parseInt(args.getOrNull(2))

                    var data: WarpPlayer;
                    var save = false;

                    if (player is Player && args.getOrNull(3) == null) data = getDataStore().getPlayer(player)!!
                    else if (args.getOrNull(3) != null) {

                        var tempPlayer = Bukkit.getOfflinePlayer(args[3])
                        if (tempPlayer == null) {
                            sendText(player, getLangMessage("command.error.player.existence", args[3]))
                            playSound(player, "command.error")
                            return false
                        }
                        if (!tempPlayer.isOnline) {
                            data = getDataStore().loadPlayer(tempPlayer)!!
                            save = true
                        } else {
                            data = getDataStore().getPlayer(tempPlayer)!!
                        }

                    } else {
                        sendText(player, getLangMessage("command.error.usage", "/transmitgens slot (add/remove) <number> <player>"))
                        playSound(player, "command.error")
                        return false
                    }


                    if (args[1].equals("add", ignoreCase = true)) {
                        data.maxGenSlots += num
                    } else {
                        data.maxGenSlots -= num
                    }
                    if (!save) getDataStore().setPlayer(Bukkit.getPlayer(data.UUID)!!, data)
                    else getDataStore().saveWarpPlayer(data);

                    return true
                } else if (args[1].equals("amount", ignoreCase = true)) {
                    if (args.getOrNull(2) == null) {
                        sendText(player, getLangMessage("command.error.usage", "/transmitgens slots amount <player>"))
                        playSound(player, "command.error")
                        return false
                    }
                    var tempPlayer = Bukkit.getOfflinePlayer(args[2])
                    if (tempPlayer == null) {
                        sendText(player, getLangMessage("command.error.player.existence", args[2]))
                        playSound(player, "command.error")
                        return false
                    }
                    var data = if (!tempPlayer.isOnline) {
                        getDataStore().loadPlayer(tempPlayer)
                    } else {
                        getDataStore().getPlayer(tempPlayer)!!
                    }!!
                    sendText(player, getLangMessage("command.slots.amount", tempPlayer.name!!, data.maxGenSlots.toString(), data.placedGenSlots.toString()))
                    return true
                }
            }
            "reset" -> {
                if (args.getOrNull(1) == null) {
                    sendText(player, getLangMessage("command.error.usage", "/transmitgens reset <player>"))
                    playSound(player, "command.error")
                    return false
                }

                var tempPlayer = Bukkit.getOfflinePlayer(args[1])
                if (tempPlayer == null) {
                    sendText(player, getLangMessage("command.error.player.existence", args[1]))
                    playSound(player, "command.error")
                    return false
                }
                var offline = false
                var data = if (!tempPlayer.isOnline) {
                    getDataStore().loadPlayer(tempPlayer)
                    offline = true
                } else {
                    getDataStore().getPlayer(tempPlayer)!!
                } as WarpPlayer
                data.placedGens.forEach { (key, locations) ->
                    locations.forEach {
                        NBTBlock(it.block).data.removeKey("TransmitNBT")
                    }
                }
                if (!offline) {
                    getDataStore().setPlayer(tempPlayer as Player, WarpPlayer(tempPlayer))
                } else {
                    getDataStore().deletePlayerFile(tempPlayer)
                }
                sendText(player, getLangMessage("command.reset.success", tempPlayer.name!!))
                playSound(player, "command.success")

            }
            "genlist", "list", "gens" -> {
                if (player is Player) {
                    var genList = GenList()
                    genList.show(player)
                }
            }
            "upgrade" -> {
                if (player is Player) {
                    player.openInventory(GenUpgradeGUI(player))
                }
            }
            "reload" -> {
                if (!player.hasPermission("transmitgens.command.reload")) {
                    sendText(player, getLangMessage("command.error.permission"))
                    playSound(player, "command.error")
                    return false
                }

                var startTime = System.currentTimeMillis()

                TransmitGenerators.getInstance().unload()
                TransmitGenerators.getInstance().load()

                var endTime = System.currentTimeMillis()

                var diffTime = endTime - startTime

                sendText(player, getLangMessage("command.reload", diffTime.toString()))
                playSound(player, "command.success")

            }
            else -> {
                if (player is Player) {
                    sendText(player, getLangMessage("command.error.existence", args.getOrNull(0) ?: "none"))
                    playSound(player, "command.error")
                    return false
                }
                TransmitGenerators.getInstance().logger.info("Incorrect Usage of command")
            }

        }

        return true
    }
}

class GenTabCommand: TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        //create new array
        val completions = ArrayList<String>()
        if (args.getOrNull(1) == null) {
            completions.add("give")
            completions.add("slot")
            completions.add("slots")
            completions.add("genlist")
            completions.add("list")
            completions.add("reload")
            completions.add("reset")
        } else if (args.getOrNull(0).equals("give", ignoreCase = true)) {
            if (args.getOrNull(2) != null) {
                Bukkit.getOnlinePlayers().forEach {
                    completions.add(it.name)
                }
            } else {
                val gens = getDataStore().getAllGenerators()
                for (gen in gens) {
                    gens.get(gen.key)?.let { completions.add(it.id) }
                }
            }

        } else if (args.getOrNull(0).equals("slot", ignoreCase = true) || args.getOrNull(0).equals("slots", ignoreCase = true)) {
            if (args.getOrNull(2) != null) {
                if (args.getOrNull(1).equals("amount", ignoreCase = true) && args.getOrNull(3) == null) {
                    Bukkit.getOnlinePlayers().forEach {
                        completions.add(it.name)
                    }
                } else {
                    if (args.getOrNull(3) != null) {
                        if (args.getOrNull(4) == null)  {
                            Bukkit.getOnlinePlayers().forEach {
                                completions.add(it.name)
                            }
                        }
                    } else {
                        for (i in 1..10) {
                            completions.add(i.toString())
                        }
                    }

                }
            } else {
                completions.add("add")
                completions.add("remove")
                completions.add("amount")
                completions.add("set")
            }

        } else if (args.getOrNull(0).equals("reset", ignoreCase = true)) {
            if (args.getOrNull(2) != null) {
                Bukkit.getOnlinePlayers().forEach {
                    completions.add(it.name)
                }
            }
        }

        completions.sort()
        return completions
    }

}
