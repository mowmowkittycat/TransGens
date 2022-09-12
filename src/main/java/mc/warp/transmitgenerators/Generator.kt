package mc.warp.transmitgenerators

import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.type.WarpItem
import org.bukkit.inventory.ItemStack

class Generator {

    var id: String;
    var block: WarpItem;
    var drop: WarpItem;


    var worth: Int;
    var price: Int;
    var upgrade: String

    constructor(name: String, block: WarpItem, drop: WarpItem, value: Int, upgrade: String, price: Int) {

        this.id = name
        this.worth = value
        this.price = price
        this.upgrade = upgrade

        this.block = block
        this.drop = drop





        TransmitGenerators.getDataStore().setGenerator(this.id, this);
    }

    constructor(name: String, blockType: ItemStack, dropType: ItemStack, value: Int, upgrade: String, price: Int): this(
        name,
        WarpItem(blockType.type, 1, "{}", blockType.type.name, ArrayList()),
        WarpItem(dropType.type, 1, "{}", blockType.type.name, ArrayList()),
        value,
        upgrade,
        price
    )




    fun getBlock(): ItemStack {
        var block = NBTItem(this.block.getItem())
        block.getOrCreateCompound("TransmitNBT").setString("generator", this.id);
        return block.item
    }
    fun getDrop(): ItemStack {
        var drop = NBTItem(this.drop.getItem())
        drop.getOrCreateCompound("TransmitNBT").setInteger("sellValue", this.worth);
        return drop.item
    }






}
