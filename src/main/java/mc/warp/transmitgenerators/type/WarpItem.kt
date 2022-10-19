package mc.warp.transmitgenerators.type

import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.Format.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class WarpItem : Cloneable {

    var item: Material;
    var amount: Int;
    var NBT: String;
    var Name: String;
    var Lore: ArrayList<String>;
    @Transient private var trueItem: ItemStack;

    override public fun clone(): WarpItem {
        return super.clone() as WarpItem
    }

    constructor(item: Material, amount: Int, NBT: String, Name: String, Lore: ArrayList<String>) {
        this.amount = amount
        this.item = item;
        this.NBT = NBT
        this.Name = Name;
        this.Lore = Lore;

        this.trueItem = gettrueItem()
    }

    fun getItem(): ItemStack {
        if (this.trueItem == null) this.trueItem = gettrueItem()
        return this.trueItem
    }

    private fun gettrueItem(): ItemStack {
        TransmitGenerators.getInstance().logger.info("Loading Item: " + Name)
        var result = ItemStack(item)
        result.amount = amount
        var nbtItem = NBTItem(result)
        var meta = result.itemMeta
        var name = text(Name)
        var lore = ArrayList<Component>()
        for (line in Lore) {
            lore.add(text(line))
        }
        var display = NBTContainer("{Lore:[]}")
        var NBTname = display.setString("Name", GsonComponentSerializer.gson().serialize(name))
        var NBTlore = display.getStringList("Lore");
        lore.forEach {
            NBTlore.add(GsonComponentSerializer.gson().serialize(it))
        }
        var realDisplay = nbtItem.addCompound("display")
        realDisplay.mergeCompound(display)

        return nbtItem.item
    }


}