package mc.warp.transmitgenerators.guis

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Format.formatValue
import mc.warp.transmitgenerators.utils.Messages
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
import mc.warp.transmitgenerators.utils.Messages.getLangMessages
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File
import java.util.*


class GenList {

    private var gui: ChestGui
    private lateinit var page: PaginatedPane
    private lateinit var title: String


    constructor() {
        gui = ChestGui.load(this, File(TransmitGenerators.getInstance().dataFolder.absolutePath + "/guis/genList.xml").inputStream())!!


        gui.setOnGlobalClick {
            it.isCancelled = true
        }

        gui.panes.forEach {
           if (it is PaginatedPane) {
               var genList = ArrayList<GuiItem>()
               page = it

               this.title = gui.title
               gui.title = title + " (" + (page.page + 1) + ") "


               var genNum = 1


               TransmitGenerators.genList.forEach {

                   var item = it.getBlock()

                   var meta = item.itemMeta

                   var nbtItem = NBTItem(item)

                   var display = NBTContainer("{Lore:[]}")
                   var upgradeName = it.upgrade.split("_")
                   var upgrade = upgradeName.map {
                       it.substring(0,1).toUpperCase() + it.substring(1).toLowerCase()
                   }
                   var lore = getLangMessages("gui.genlist.generatorLore",formatValue(it.worth.toDouble()), upgrade.joinToString(" ") ,formatValue(it.price.toDouble()),genNum.toString())

                   var NBTlore = display.getStringList("Lore");

                   display.setString("Name", GsonComponentSerializer.gson().serialize(getLangMessage("gui.genlist.generatorName", it.block.Name, genNum.toString())!!))
                   lore.forEach {
                       NBTlore.add(GsonComponentSerializer.gson().serialize(it))
                   }
                   var realDisplay = nbtItem.addCompound("display")
                   realDisplay.mergeCompound(display)

                   item = nbtItem.item

                   genList.add(GuiItem(item));

                   genNum += 1



               }

               it.populateWithGuiItems(genList)
           }

        }
    }


    fun goBackClick(event: InventoryClickEvent) {
        if ((page.page + 1) > 1) {
            page.page -= 1
            gui.title = title + " (" + (page.page + 1) + ") "
            gui.update()
        } else {
            Messages.playSound(event.whoClicked as Player, "command.error")
        }
    }
    fun nextPageClick(event: InventoryClickEvent) {
        if (page.page < page.pages) {
            page.page = page.page + 1
            gui.title = title + " (" + (page.page + 1) + ") "
            gui.update()
        } else {
            Messages.playSound(event.whoClicked as Player, "command.error")
        }



    }

    fun show(viewer: HumanEntity) {
        gui.show(viewer);
    }

}