package mc.warp.transmitgenerators


import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.type.WarpItem
import mc.warp.transmitgenerators.utils.Format
import mc.warp.transmitgenerators.utils.Messages.getMessage
import mc.warp.transmitgenerators.utils.Messages.getMessages
import org.bukkit.inventory.ItemStack

class Generator {

    var id: String;
    var block: WarpItem;
    var drop: WarpItem;
    @Transient var trueBlock: WarpItem;
    @Transient var trueDrop: WarpItem


    var worth: Long;
    var price: Long;
    var upgrade: String

    constructor(name: String, block: WarpItem, drop: WarpItem, value: Long, upgrade: String, price: Long) {

        this.id = name
        this.worth = value
        this.price = price
        this.upgrade = upgrade

        this.block = block.clone()

        var nbt = NBTContainer(block.NBT)
        nbt.getOrCreateCompound("TransmitNBT").setString("generator", this.id);
        drop.NBT = nbt.toString()

        if (getDataStore().config.getBoolean("generator-boilerplate-name")) {
            block.Name = getMessage("generators.boilerplate.generatorName", block.Name)!!
        }
        if (getDataStore().config.getBoolean("generator-boilerplate-lore")) {
            var upgradeName = upgrade.split("_")
            var upgrade = upgradeName.map {
                it.substring(0,1).toUpperCase() + it.substring(1).toLowerCase()
            }
            block.Lore = getMessages("generators.boilerplate.generatorLore",
                Format.formatValue(worth.toDouble()), upgrade.joinToString(" ") ,
                Format.formatValue(price.toDouble())
            )
        }
        this.trueBlock = block

        this.drop = drop.clone()

        var nbt2 = NBTContainer(drop.NBT)
        nbt2.getOrCreateCompound("TransmitNBT").setInteger("sellValue", this.worth.toInt());
        drop.NBT = nbt2.toString()
        if (getDataStore().config.getBoolean("drops-boilerplate-name")) {
            drop.Name = getMessage("generators.boilerplate.dropName", drop.Name)!!
        }
        if (getDataStore().config.getBoolean("drops-boilerplate-lore")) {
            drop.Lore = getMessages("generators.boilerplate.dropLore", Format.formatValue(worth.toDouble()))
        }
        this.trueDrop = drop






         getDataStore().setGenerator(this.id, this);
    }

    constructor(name: String, blockType: ItemStack, dropType: ItemStack, value: Long, upgrade: String, price: Long): this(
        name,
        WarpItem(blockType.type, 1, "{}", blockType.type.name, ArrayList()),
        WarpItem(dropType.type, 1, "{}", blockType.type.name, ArrayList()),
        value,
        upgrade,
        price
    )




    fun getBlock(): ItemStack {
        var block = NBTItem(this.trueBlock.getItem())
        block.getOrCreateCompound("TransmitNBT").setString("generator", this.id);
        return block.item
    }
    fun getDrop(): ItemStack {
        var drop = NBTItem(this.trueDrop.getItem())
        drop.getOrCreateCompound("TransmitNBT").setInteger("sellValue", this.worth.toInt());
        return drop.item
    }







}
