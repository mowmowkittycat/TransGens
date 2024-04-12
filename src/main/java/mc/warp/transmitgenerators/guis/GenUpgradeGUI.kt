package mc.warp.transmitgenerators.guis

import com.idlegens.idlecore.NumberFormat
import com.idlegens.idlecore.gui.InventoryCanvas
import com.idlegens.idlecore.gui.InventoryGUI
import com.idlegens.idlecore.gui.InventoryManager
import com.idlegens.idlecore.player.PlayerManager
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.events.GenEvents
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Messages
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.floor

class GenUpgradeGUI(var player: Player) : InventoryGUI() {

    var mode = UpgradeMode.SINGLE
    override fun getSize() = 6
    override fun getTitle(): String {
        return "Generator Upgrade (${TransmitGenerators.getDataStore().playerList[player.uniqueId]!!.placedGenSlots})"
    }

    override fun render(player: Player, canvas: InventoryCanvas) {

        var warpPlayer = TransmitGenerators.getDataStore().playerList[player.uniqueId]!!
        var generatorList = TransmitGenerators.getDataStore().getAllGenerators()

        var num = 10
        var generators = warpPlayer.placedGens
        for (i in 0..36) {
            canvas.clearSlot(i)
        }
        for ( (type, locations) in generators ) {
            if (locations.isEmpty()) continue
            if ((num % 9) == 8) {
                num += 2
            }
            var genName = generatorList[type]!!.block.Name.split("_").map {
                it.substring(0,1).toUpperCase() + it.substring(1).toUpperCase()
            }.joinToString(" ")

            var genAmount: Int = 0
            var perOneCost = generatorList[type]!!.price.toDouble()
            var price: Double = 0.0
            if (mode == UpgradeMode.SINGLE) {
                genAmount = 1
            }
            if (mode == UpgradeMode.ALL) {
                genAmount = locations.size
            }
            if (mode == UpgradeMode.MAX) {
                genAmount = floor(PlayerManager.getPlayerData(player).coins / perOneCost).toInt()
            }
            if (genAmount <= 0) genAmount = 1
            if (genAmount > locations.size) genAmount = locations.size
            price = genAmount * perOneCost


            canvas.slot(num) {
                material = generatorList[type]!!.getBlock().type
                name = "<#ffdb4a><bold>$genName</bold> <dark_gray>(${locations.size})"
                amount = locations.size
                lore = arrayListOf(
                    "",
                    "<gray>Left-click this to upgrade generators",
                    "<gray> that are of the same type",
                    "",
                    "<white>Current Mode: <#ffdb4a><bold>${mode.toString()}</bold>",
                    "",
                    "<gold><bold>COST</bold>:",
                    "<dark_gray> • <gold>⛃ <yellow>${NumberFormat.bigFormat(price)}",
                    "",
                    "<#ffdb4a><bold>LEFT-CLICK</bold> to Upgrade!"
                )
                onClick = {


                    var coinFormatted = NumberFormat.bigFormat(PlayerManager.getPlayerData(player).coins)
                    if (genAmount <= 0) {
                        Messages.playSound(player, "gen.error.poor")
                        Format.sendTitle(
                            player,
                            Messages.getLangMessage("gen.error.poor.title"),
                            Messages.getLangMessage(
                                "gen.error.poor.subtitle",
                                coinFormatted,
                                NumberFormat.bigFormat(perOneCost)
                            ),
                            200L,
                            200L,
                            1500L
                        )
                        player.closeInventory()
                    }
                    if (PlayerManager.getPlayerData(player).coins >= price) {
                        var cloneList = locations.clone() as ArrayList<Location>
                        for (i in 0..genAmount - 1) {
                            GenEvents.upgradeGenerator(player, cloneList[i], false)
                        }
                        InventoryManager.reRenderInventory(player)
                        Messages.playSound(player, "gen.sound.upgradeGen")
                    } else {
                        Messages.playSound(player, "gen.error.poor")
                        Format.sendTitle(
                            player,
                            Messages.getLangMessage("gen.error.poor.title"),
                            Messages.getLangMessage(
                                "gen.error.poor.subtitle",
                                coinFormatted,
                                NumberFormat.bigFormat(price)
                            ),
                            200L,
                            200L,
                            1500L
                        )
                        player.closeInventory()
                    }
                }
            }


            num++
        }

        canvas.slot(39) {
            material = Material.EMERALD
            name = "<green><bold>UPGRADE SINGLE MODE</bold></green> <dark_gray>(Left-Click)"
            shiny = (mode == UpgradeMode.SINGLE)
            lore = arrayListOf(
                "",
                "<gray>This mode will make you upgrade one",
                "<gray>random generator when you click",
                "",
                "<white>Current Mode: <green><bold>${mode.toString()}</bold>",
                "",
                "<green><bold>LEFT-CLICK</bold> to change to single mode!"
            )
            onClick = {
                mode = UpgradeMode.SINGLE
                InventoryManager.reRenderInventory(player)
            }
        }
        canvas.slot(40) {
            material = Material.EMERALD_ORE
            name = "<green><bold>UPGRADE MAX MODE</bold></green> <dark_gray>(Left-Click)"
            shiny = (mode == UpgradeMode.MAX)
            lore = arrayListOf(
                "",
                "<gray>This mode will make you upgrade as many",
                "<gray>generators as you can when you click",
                "",
                "<white>Current Mode: <green><bold>${mode.toString()}</bold>",
                "",
                "<green><bold>LEFT-CLICK</bold> to change to single mode!"
            )
            onClick = {
                mode = UpgradeMode.MAX
                InventoryManager.reRenderInventory(player)
            }
        }
        canvas.slot(41) {
            material = Material.EMERALD_BLOCK
            name = "<green><bold>UPGRADE ALL MODE</bold></green> <dark_gray>(Left-Click)"
            shiny = (mode == UpgradeMode.ALL)
            lore = arrayListOf(
                "",
                "<gray>This mode will make you upgrade all",
                "<gray>generators when you click",
                "",
                "<white>Current Mode: <green><bold>${mode.toString()}</bold>",
                "",
                "<green><bold>LEFT-CLICK</bold> to change to single mode!"
            )
            onClick = {
                mode = UpgradeMode.ALL
                InventoryManager.reRenderInventory(player)
            }
        }
    }
}