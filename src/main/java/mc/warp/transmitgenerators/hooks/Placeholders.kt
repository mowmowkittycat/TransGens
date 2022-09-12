package mc.warp.transmitgenerators.hooks

import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


class Placeholders : PlaceholderExpansion {
    private var plugin: TransmitGenerators;

    constructor(plugin: TransmitGenerators) {
        this.plugin = plugin
    }

    override fun getAuthor(): String {
        return "MrEnxo"
    }

    override fun getIdentifier(): String {
        return "transmitgens"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        if (params.equals("get_genAmount", ignoreCase = true)) {
            var playerData = getDataStore().getPlayer(player as Player) ?: return null
            return playerData.placedGens.size.toString()
        }
        if (params.equals("get_maxGenAmount", ignoreCase = true)) {
            var playerData = getDataStore().getPlayer(player as Player) ?: return null
            return playerData.maxGenSlots.toString()
        }
        return null
    }
}