package mc.warp.transmitgenerators.guis

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Messages
import mc.warp.transmitgenerators.utils.Messages.getLangMessage
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
               gui.title = title + " (" + page.page + ") "


               var genNum = 1

               TransmitGenerators.getDataStore().getAllGenerators().forEach {
                   var key = it.key
                   var value = it.value

                   var item = value.getBlock()

                   var meta = item.itemMeta

                   var nbtItem = NBTItem(item)

                   var display = NBTContainer("{Lore:[]}")
                   var lore = TransmitGenerators.getDataStore().messages["gui.genlist.generatorLore"]!!
                   var Objects = arrayListOf<String>(value.worth.toString(), value.upgrade, value.price.toString())
                   var num = 1
                   for (replacement in Objects) {
                       lore = lore.replace("$${num}", replacement, true)
                       num++
                   }
                   var loreList = lore.split("\n")
                   var NBTlore = display.getStringList("Lore");
                   var NBTname = display.setString("Name", GsonComponentSerializer.gson().serialize(getLangMessage("gui.genlist.generatorName", value.block.Name, genNum)!!))
                   loreList.forEach {
                       NBTlore.add(GsonComponentSerializer.gson().serialize(Format.formater.deserialize(it)))
                   }
                   var realDisplay = nbtItem.addCompound("display")
                   realDisplay.mergeCompound(display)

                   item = nbtItem.item

                   genList.add(GuiItem(item))

                   genNum += 1
               }




               it.populateWithGuiItems(genList);
           }

        }
    }


    fun goBackClick(event: InventoryClickEvent) {
        if (page.page > 1) {
            page.page -= 1
            gui.title = title + " (" + page.page + ") "
            gui.update()
        } else {
            Messages.playSound(event.whoClicked as Player, "command.error")
        }
    }
    fun nextPageClick(event: InventoryClickEvent) {
        if (page.pages < page.page) {
            page.page = page.page + 1
            gui.title = title + " (" + page.page + ") "
            gui.update()
        } else {
            Messages.playSound(event.whoClicked as Player, "command.error")
        }



    }

    fun show(viewer: HumanEntity) {
        gui.show(viewer);
    }

}