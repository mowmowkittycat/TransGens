package mc.warp.transmitgenerators.events

import com.idlegens.idlecore.NumberFormat
import com.idlegens.idlecore.player.PlayerManager
import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Format.formatValue
import mc.warp.transmitgenerators.utils.Format.formater
import mc.warp.transmitgenerators.utils.Format.sendText
import mc.warp.transmitgenerators.utils.Format.sendTitle
import mc.warp.transmitgenerators.utils.Format.text
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
import mc.warp.transmitgenerators.utils.Messages.playSound
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.reflect.typeOf


object GenEvents: Listener {

    @EventHandler
    fun onGenPlace(e: BlockPlaceEvent) {
        var tool = e.itemInHand
        var player = e.player;
        var playerData = getDataStore().getPlayer(player) ?: return
        if (tool.type == Material.AIR || tool == null) {
            e.isCancelled = true
            return
        }
        var nbt = NBTItem(tool).getCompound("TransmitNBT")

        if (!generatorCheck(nbt, player)) return

        if (playerData.placedGenSlots >= playerData.maxGenSlots) {
            e.isCancelled = true
            playSound(player, "gen.error.maxGenSlot")
            sendTitle(
                player,
                getLangMessage("gen.error.maxGenSlots.title"),
                getLangMessage("gen.error.maxGenSlots.subtitle",
                    playerData.maxGenSlots.toString()),
                200L,
                200L,
                200L
            )


        }

        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {

            var nbt = NBTItem(tool).getCompound("TransmitNBT")

            if (!generatorCheck(nbt, player)) return@schedule


            var playerData = getDataStore().getPlayer(player) ?: return@schedule
            var generator = getDataStore().getGenerator(nbt!!.getString("generator")) ?: return@schedule
            waitFor(1)

            if (e.isCancelled) return@schedule



            var nbtblock = NBTBlock(e.block)
            var compound = nbtblock.data.getOrCreateCompound("TransmitNBT")
            compound.setString("generator", generator.id);
            compound.setString("owner", player.uniqueId.toString());
            playSound(player, "gen.sound.placeGen")
            sendTitle(
                player,
                getLangMessage("gen.message.placeGen.title"),
                getLangMessage("gen.message.placeGen.subtitle",
                    (playerData.placedGenSlots + 1).toString(),
                    playerData.maxGenSlots.toString()),
                200L,
                200L,
                200L
            )
            var gens = playerData.placedGens.getOrPut(generator.id) { ArrayList() }
            gens.add(e.block.location)
            playerData.placedGenSlots++;

        }

    }


    @EventHandler
    fun onGenBreak(e: BlockBreakEvent) {
        var nbt = NBTBlock(e.block).data.getCompound("TransmitNBT")

        if (!generatorCheck(nbt, e.player)) return
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            waitFor(1)
            if (e.isCancelled) return@schedule
            breakGenerator(e.player, e.block, e);

        }
    }


    private fun breakGenerator(player: Player, block: Block, e: Cancellable) {
        var player = player;
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")


        if (!generatorCheck(nbt, player)) return

        var playerData = getDataStore().getPlayer(player) ?: return

        if (!generatorPermissionCheck(nbt, player)) {
            e.isCancelled = true
            return
        }
        var generator = getDataStore().getGenerator(nbt!!.getString("generator")) ?: return

        var gens = playerData.placedGens.getOrPut(generator.id) { ArrayList() }


        if (gens.contains(block.location)) {
            playSound(player, "gen.sound.breakGen")
            sendTitle(player,
                getLangMessage("gen.message.breakGen.title"),
                getLangMessage("gen.message.breakGen.subtitle",
                    (playerData.placedGenSlots  - 1).toString(),
                    playerData.maxGenSlots.toString()),
                200L,
                200L,
                200L)
            gens.remove(block.location)
            player.inventory.addItem(generator.getBlock())
            playerData.ephemeralBlocks[block.location] = true;
            block.type = Material.AIR

            playerData.placedGenSlots--;

            var nbt = NBTBlock(block)
            nbt.data.removeKey("TransmitNBT");


            return
        }
    }


    @EventHandler
    fun onGenLeftClick(e: PlayerInteractEvent) {
        var action = e.action
        if (action != Action.LEFT_CLICK_BLOCK) return
        var player = e.player
        var block = e.clickedBlock ?: return
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")

        if (!generatorCheck(nbt, player)) return
        if (!player.isSneaking) {
            e.isCancelled = true
            playSound(player, "gen.error.sneak")
            sendTitle(player,
                getLangMessage("gen.error.sneak.title"),
                getLangMessage("gen.error.sneak.subtitle"),
                200L,
                200L,
                200L)
            return
        }

        breakGenerator(e.player,block,e);
    }

    private fun generatorCheck(nbt: NBTCompound?, player: Player): Boolean {
        if (nbt == null) return false
        if (nbt.getString("generator").isNullOrEmpty() || nbt.getString("generator").isNullOrBlank()) return false
        return true
    }

    private fun generatorPermissionCheck(nbt: NBTCompound?, player: Player): Boolean {
        if (nbt == null) return false
        if (nbt.getString("owner") != player.uniqueId.toString()) {
            playSound(player, "gen.error.permission")
            sendTitle(player,
                getLangMessage("gen.error.permission.title"),
                getLangMessage("gen.error.permission.subtitle"),
                200L,
                200L,
                200L)
            return false
        }
        return true
    }

    fun upgradeGenerator(player: Player, location: Location, sendMessages: Boolean = true, event: Cancellable? = null) {
        var block = location.block ?: return
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")

        if (!generatorCheck(nbt, player)) return
        if (!generatorPermissionCheck(nbt, player)) return
        var playerData = getDataStore().getPlayer(player) ?: return
        var generator = getDataStore().getGenerator(nbt!!.getString("generator")) ?: return
        var upgrade = getDataStore().getGenerator(generator.upgrade)


        if (upgrade == null) {
            if (sendMessages) playSound(player, "gen.error.maxGenLevel")
            if (sendMessages) sendTitle(player,
                getLangMessage("gen.error.maxGenLevel.title"),
                getLangMessage("gen.error.maxGenLevel.subtitle"),
                200L,
                200L,
                200L)
            if (event != null) event.isCancelled = true
            return
        }

        if (PlayerManager.getPlayerData(player).coins < generator.price.toDouble()) {
            if (sendMessages) playSound(player, "gen.error.poor")
            if (sendMessages) sendTitle(player,
                getLangMessage("gen.error.poor.title"),
                getLangMessage("gen.error.poor.subtitle",
                    NumberFormat.bigFormat(PlayerManager.getPlayerData(player).coins ?: 0.0),
                    NumberFormat.bigFormat(generator.price.toDouble())),
                200L,
                200L,
                500L)
            if (event != null) event.isCancelled = true
            return
        }
        PlayerManager.getPlayerData(player).coins -= generator.price.toDouble()
        block.type = upgrade.getBlock().type
        NBTBlock(block).data.getCompound("TransmitNBT")!!.setString("generator", upgrade.id);
        if (sendMessages) playSound(player, "gen.sound.upgradeGen")
        if (sendMessages) sendTitle(player,
            getLangMessage("gen.message.upgradeGen.title"),
            getLangMessage("gen.message.upgradeGen.subtitle",
                generator.id,
                upgrade.id),
            200L,
            200L,
            200L)

        var gens = playerData.placedGens.getOrPut(generator.id) { ArrayList() }
        var upgradeGens = playerData.placedGens.getOrPut(upgrade.id) { ArrayList() }
        gens.remove(block.location)
        upgradeGens.add(block.location)

    }

    @EventHandler
    fun onGenRightClick(e: PlayerInteractEvent) {
        var action = e.action
        if (action != Action.RIGHT_CLICK_BLOCK) return
        var player = e.player
        var tool = player.inventory.itemInMainHand
        var playerData = getDataStore().getPlayer(player) ?: return


        if (!player.isSneaking) return
        if (tool.type != Material.AIR) {
                var toolNbt = NBTItem(tool).getCompound("TransmitNBT")
                if (toolNbt != null) {
                    if (toolNbt.getString("generator") != null) return
                }
        }
        e.isCancelled = true

        if (!playerData.canUpgrade) return
        playerData.canUpgrade = false
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            waitFor(3)
            playerData.canUpgrade = true
        }


        upgradeGenerator(player, e.clickedBlock!!.location,true , e)


    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        var player = e.player
        getDataStore().firstPlayerLoad(player)

    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        var player = e.player
        getDataStore().savePlayer(player)
        getDataStore().playerList.remove(player.uniqueId)

    }



}


