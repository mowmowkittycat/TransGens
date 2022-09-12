package mc.warp.transmitgenerators.events

import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.utils.Format.sendText
import mc.warp.transmitgenerators.utils.Format.sendTitle
import mc.warp.transmitgenerators.utils.Format.text
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
import mc.warp.transmitgenerators.utils.Messages.playSound
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
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


class GenEvents: Listener {

    @EventHandler
    fun onGenPlace(e: BlockPlaceEvent) {
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            var player = e.player;
            var tool = player.inventory.itemInMainHand
            var nbt = NBTItem(tool).getCompound("TransmitNBT");

            if (nbt == null) return@schedule

            var playerData = getDataStore().getPlayer(player) ?: return@schedule
            var generator = getDataStore().getGenerator(nbt.getString("generator")) ?: return@schedule
            waitFor(1)
            if (e.isCancelled) return@schedule

            if (playerData.placedGens.size >= playerData.maxGenSlots) {
                playSound(player, "gen.error.maxGenSlot")
                sendTitle(
                    player,
                    getLangMessage("gen.error.maxGenSlots.title"),
                    getLangMessage("gen.error.maxGenSlots.subtitle", playerData.maxGenSlots),
                    200L,
                    200L,
                    200L
                )
                e.isCancelled = true
                return@schedule
            }

            var nbtblock = NBTBlock(e.block)
            var compound = nbtblock.data.getOrCreateCompound("TransmitNBT")
            compound.setString("generator", generator.id);
            compound.setString("owner", player.uniqueId.toString());
            playSound(player, "gen.sound.placeGen")
            sendTitle(
                player,
                getLangMessage("gen.message.placeGen.title"),
                getLangMessage("gen.message.placeGen.subtitle", playerData.placedGens.size + 1, playerData.maxGenSlots),
                200L,
                200L,
                200L
            )
            playerData.placedGens.add(e.block.location)
        }

    }


    @EventHandler
    fun onGenBreak(e: BlockBreakEvent) {
        var nbt = NBTBlock(e.block).data.getCompound("TransmitNBT")

        if (nbt == null) return
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            waitFor(1)
            if (e.isCancelled) return@schedule
            breakGenerator(e.player, e.block, e);

        }
    }


    fun breakGenerator(player: Player, block: Block, e: Cancellable) {
        var player = player;
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")


        if (nbt == null) return
        if (nbt.getString("generator") == null) return

        var playerData = getDataStore().getPlayer(player) ?: return

        if (nbt.getString("owner") != player.uniqueId.toString()) {
            playSound(player, "gen.error.maxGenSlot")
            sendTitle(player, getLangMessage("gen.error.maxGenSlots.title"),getLangMessage("gen.error.maxGenSlots.subtitle", playerData.maxGenSlots), 200L, 200L, 200L)
            e.isCancelled = true
            return
        }

        var generator = getDataStore().getGenerator(nbt.getString("generator")) ?: return

        for (gen in playerData.placedGens) {
            if (gen.toVector().equals(block.location.toVector()) && gen.world.equals(block.world)) {
                playSound(player, "gen.sound.breakGen")
                sendTitle(player, getLangMessage("gen.message.breakGen.title"),getLangMessage("gen.message.breakGen.subtitle", playerData.placedGens.size - 1,playerData.maxGenSlots), 200L, 200L, 200L)
                playerData.placedGens.remove(gen);
                player.inventory.addItem(generator.getBlock())
                block.setType(Material.AIR)
                return
            }
        }
    }


    @EventHandler
    fun onGenLeftClick(e: PlayerInteractEvent) {
        var action = e.action
        if (action != Action.LEFT_CLICK_BLOCK) return
        var player = e.player
        var block = e.clickedBlock ?: return
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")


        if (nbt == null) return
        if (player.isSneaking == false) {
            e.isCancelled = true
            playSound(player, "gen.error.sneak")
            sendTitle(player, getLangMessage("gen.error.sneak.title"),getLangMessage("gen.error.sneak.subtitle"), 200L, 200L, 200L)
            return
        }

        breakGenerator(e.player,block,e);
    }

    @EventHandler
    fun onGenRightClick(e: PlayerInteractEvent) {
        var action = e.action
        if (action != Action.RIGHT_CLICK_BLOCK) return
        var player = e.player
        var tool = player.inventory.itemInMainHand
        var block = e.clickedBlock ?: return
        var nbt = NBTBlock(block).data.getCompound("TransmitNBT")


        if (nbt == null) return
        if (player.isSneaking == false) return
        if (tool.type != Material.AIR) {
                var toolNbt = NBTItem(tool).getCompound("TransmitNBT")
                if (toolNbt != null) {
                    if (toolNbt.getString("generator") != null) return
                }
        }

        var playerData = getDataStore().getPlayer(player) ?: return
        var generator = getDataStore().getGenerator(nbt.getString("generator")) ?: return
        var upgrade = getDataStore().getGenerator(generator.upgrade)
        if (!playerData.canUpgrade) return
        playerData.canUpgrade = false
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            waitFor(3)
            playerData.canUpgrade = true
        }

        if (upgrade == null) {
            playSound(player, "gen.error.maxGenLevel")
            sendTitle(player, getLangMessage("gen.error.maxGenLevel.title"),getLangMessage("gen.error.maxGenLevel.subtitle"), 200L, 200L, 200L)
            e.isCancelled = true
            return
        }

        if (!(TransmitGenerators.econ.getBalance(player) >= generator.price.toDouble())) {
            playSound(player, "gen.error.poor")
            sendTitle(player, getLangMessage("gen.error.poor.title"),getLangMessage("gen.error.poor.subtitle",TransmitGenerators.econ.getBalance(player), generator.price), 200L, 200L, 500L)
            e.isCancelled = true
            return
        }
        TransmitGenerators.econ.withdrawPlayer(player, generator.price.toDouble())

        playerData.canUpgrade = false
        block.type = upgrade.getBlock().type
        NBTBlock(block).data.getCompound("TransmitNBT").setString("generator", upgrade.id);
        playSound(player, "gen.sound.upgradeGen")
        sendTitle(player, getLangMessage("gen.message.upgradeGen.title"),getLangMessage("gen.message.upgradeGen.subtitle", generator.id, upgrade.id), 200L, 200L, 200L)



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


