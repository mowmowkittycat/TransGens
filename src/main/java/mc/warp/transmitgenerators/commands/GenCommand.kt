package mc.warp.transmitgenerators.commands

import mc.warp.transmitgenerators.Generator
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.guis.GenList
import mc.warp.transmitgenerators.utils.Format.sendText
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
import mc.warp.transmitgenerators.utils.Messages.playSound
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*


class GenCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        var player = sender as Player

        if (!player.hasPermission("transmitgens.command")) {
            sendText(player, getLangMessage("command.error.permission"))
            playSound(player, "command.error")
            return false
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
                    sendText(player, getLangMessage("command.error.gen.existence",args.get(1)))
                    playSound(player, "command.error")
                    return false
                }
                if (args.getOrNull(2) != null) {
                    var sendPlayer = Bukkit.getPlayer(args.get(2))
                    if (sendPlayer != null) {
                        sendPlayer.inventory.addItem(gen.getBlock())
                        playSound(player, "command.success")
                    } else {
                        sendText(player, getLangMessage("command.error.player.existence",args.get(2)))
                        playSound(player, "command.error")
                        return false
                    }
                } else {
                    player.inventory.addItem(gen.getBlock())
                    playSound(player, "command.success")
                }

            }
            "slot", "slots" -> {
                if (args.getOrNull(1) != null) {
                    if (args.get(1).equals("add", ignoreCase = true) || args.get(1).equals("remove", ignoreCase = true)) {

                        if (args.getOrNull(2) == null) {
                            sendText(player, getLangMessage("command.error.usage", "/transmitgens slot (add/remove) <number> <player>"))
                            playSound(player, "command.error")
                            return false
                        }
                        var num = Integer.parseInt(args.getOrNull(2))
                        var sendPlayer = player

                        if (args.getOrNull(3) != null) {
                            var testPlayer = Bukkit.getPlayer(args.get(3))
                            if (testPlayer != null) {
                                sendPlayer = testPlayer
                            }
                        }

                        var warpPlayer = getDataStore().getPlayer(sendPlayer)!!

                        if (args.get(1).equals("add", ignoreCase = true)) {
                            warpPlayer.maxGenSlots += num
                        } else {
                            warpPlayer.maxGenSlots -= num
                        }

                        getDataStore().setPlayer(sendPlayer, warpPlayer)
                        return true
                    } else if (args.get(1).equals("amount", ignoreCase = true)) {
                        if (args.getOrNull(2) == null) {
                            sendText(player, getLangMessage("command.error.usage", "/transmitgens slots amount <player>"))
                            playSound(player, "command.error")
                            return false
                        }
                        var getPlayer = Bukkit.getPlayer(args.get(2))
                        if (getPlayer == null) {
                            sendText(player, getLangMessage("command.error.player.existence",args.get(2)))
                            playSound(player, "command.error")
                            return false
                        }
                        sendText(player, getLangMessage("command.slots.amount",getPlayer.name, getDataStore().getPlayer(getPlayer)!!.maxGenSlots))
                        return true
                    }
                }

                sendText(player, getLangMessage("command.error.usage", "/transmitgens slot (add/remove/amount)"))
                playSound(player, "command.error")
                return false

            }
            "genlist", "list", "gens" -> {
                var genList = GenList()
                genList.show(player)
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

                sendText(player, getLangMessage("command.reload", diffTime))
                playSound(player, "command.success")
            }
            else -> {
                sendText(player, getLangMessage("command.error.existence",args.getOrNull(0) ?: "none"))
                playSound(player, "command.error")
                return false
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
                } else if (args.getOrNull(1).equals("add", ignoreCase = true) || args.getOrNull(1).equals("remove", ignoreCase = true)) {
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
            }

        }

        completions.sort()
        return completions
    }

}
