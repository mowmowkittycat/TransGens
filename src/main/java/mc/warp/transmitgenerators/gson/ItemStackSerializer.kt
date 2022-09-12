package mc.warp.transmitgenerators.gson

import com.google.gson.*
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTItem
import mc.warp.transmitgenerators.TransmitGenerators
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type


class ItemStackSerializer : JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        var json = JsonObject();
        var nbtItem = NBTItem(src)

        json.add("type", JsonPrimitive(src.type.name))
        json.add("amount", JsonPrimitive(src.amount))
        json.add("nbt", JsonPrimitive(nbtItem.toString()))


        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemStack {
        var item = ItemStack(Material.DIRT)
        var JSON = json.asJsonObject ?: return item
        var type = JSON.get("type").asString ?: return item
        var amount = JSON.get("amount").asInt ?: return item

        item.type = Material.getMaterial(type)!!
        item.amount = amount

        var nbt = JSON.get("nbt").asString ?: return item
        var nbtItem = NBTItem(item)
        var parsedNBT = NBTContainer(nbt)
        nbtItem.mergeCompound(parsedNBT)
        item = nbtItem.item.clone()



        return item
    }
}