package mc.warp.transmitgenerators.utils

import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.type.WarpSound
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
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
    fun playSound(player: CommandSender, sound: String) {
        if (player is Player) {
            var Sound = getDataStore().sounds[sound]
            if (Sound != null) {
                playSound(player, Sound)
            };
        }
    }
    fun playSound(player: CommandSender, sound: WarpSound) {
        if (player is Player) {
            playSound(player, sound)
        }
    }
    fun getLangMessage(id: String): Component? {
        var message = getDataStore().messages.getString(id) ?: return null
        return Format.formater.deserialize(message)
    }
    fun getLangMessage(id: String, vararg Objects: String): Component? {
        var str = getDataStore().messages.getString(id) ?: return null
        var num = 1
        for (replacement in Objects) {
            str = str.replace("$${num}", replacement.toString(), true)
            num++
        }
        return Format.formater.deserialize(str)
    }

    fun replaceInString(string: String, Objects: MutableList<String>): String {
        var str = string
        var num = 1
        for (replacement in Objects) {
            str = str.replace("$${num}", replacement.toString(), true)
            num++
        }
        return str
    }

    fun getLangMessages(id: String): ArrayList<Component> {
        var message = getDataStore().messages.getStringList(id)
        var componentList = ArrayList<Component>()

        message.forEach {
            componentList.add(Format.formater.deserialize(it))
        }

        return componentList
    }

    fun getLangMessages(id: String, vararg Objects: String): ArrayList<Component> {
        var message = getDataStore().messages.getStringList(id)
        var messageReplaced = ArrayList<String>()
        var componentList = ArrayList<Component>()
        message.forEach {
            messageReplaced.add(replaceInString(it, Objects.toMutableList()))
        }

        messageReplaced.forEach {
            componentList.add(Format.formater.deserialize(it))
        }


        return componentList
    }

    fun getMessage(id: String, vararg Objects: String): String? {
        var message = getDataStore().messages.getString(id) ?: return null
        return replaceInString(message, Objects.toMutableList())
    }

    fun getMessages(id: String, vararg Objects: String): ArrayList<String> {
        var message = getDataStore().messages.getStringList(id)
        var messageReplaced = ArrayList<String>()
        message.forEach {
            messageReplaced.add(replaceInString(it, Objects.toMutableList()))
        }

        return messageReplaced
    }



}