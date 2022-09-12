package mc.warp.transmitgenerators.guis

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.Messages
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.io.File


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

               TransmitGenerators.getDataStore().getAllGenerators().forEach {
                   var key = it.key
                   var value = it.value

                   var item = GuiItem(value.getBlock())
                   genList.add(item)

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