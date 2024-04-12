package mc.warp.transmitgenerators.commands

import com.idlegens.idlecore.IdleCore
import com.idlegens.idlecore.player.PlayerManager
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Messages
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SellCommand : Command {

    constructor() : super("sell")

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            var player = sender as Player

            var amount = 0

            player.inventory.contents.forEach {
                if (it == null) return@forEach
                if (it.type == Material.AIR) return@forEach

                var nbtItem = NBTItem(it)
                var nbtCompound = nbtItem.getCompound("TransmitNBT") ?: return@forEach

                var sell = nbtCompound.getInteger("sellValue")

                amount += (it.amount * sell)
                if (sell > 0) it.amount = 0

            }
            Format.sendText(player, Messages.getLangMessage("command.sell.success", amount.toString()))
            Format.sendTitle(player,Messages.getLangMessage("command.sell.success.title"),Messages.getLangMessage("command.sell.success.subtitle", amount.toString()))
            Messages.playSound(player, "command.success")

            PlayerManager.getPlayerData(player).coins += amount.toDouble()



            return true
        } else {
            return false
        }
        return false
    }
}