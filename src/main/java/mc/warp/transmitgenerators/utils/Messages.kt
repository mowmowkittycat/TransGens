package mc.warp.transmitgenerators.utils

import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.type.WarpSound
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object Messages {

    fun playSound(player: Player, sound: String) {
        var Sound = getDataStore().sounds[sound]
        if (Sound != null) {
            playSound(player, Sound)
        };
    }
    fun playSound(player: Player, sound: WarpSound) {
        player.playSound(player.location,sound.soundName,sound.volume,sound.pitch);
    }
    fun getLangMessage(id: String): Component? {
        var message = getDataStore().messages[id] ?: return null
        return Format.formater.deserialize(message)
    }
    fun getLangMessage(id: String, vararg Objects: Any): Component? {
        var str = getDataStore().messages[id] ?: return null
        var num = 1
        for (replacement in Objects) {
            str = str.replace("$${num}", replacement.toString(), true)
            num++
        }
        return Format.formater.deserialize(str)
    }

}