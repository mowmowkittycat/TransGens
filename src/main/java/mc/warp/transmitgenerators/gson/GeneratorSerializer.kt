package mc.warp.transmitgenerators.gson

import com.google.gson.*
import mc.warp.transmitgenerators.DataStore
import mc.warp.transmitgenerators.Generator
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.type.WarpItem
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type

class GeneratorSerializer  : JsonSerializer<Generator>, JsonDeserializer<Generator> {
    override fun serialize(gen: Generator, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        var json = JsonObject()

        json.add("id", JsonPrimitive(gen.id))
        json.add("sellValue", JsonPrimitive(gen.worth))
        json.add("upgradePrice", JsonPrimitive(gen.price))
        json.add("upgradeId", JsonPrimitive(gen.upgrade))


        json.add("drop", TransmitGenerators.getDataStore().GSON.toJsonTree(gen.drop) )
        json.add("block", TransmitGenerators.getDataStore().GSON.toJsonTree(gen.block))

        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Generator {
        var json = json.asJsonObject


        var id = json.get("id").asString!!
        var worth = json.get("sellValue").asLong!!
        var price = json.get("upgradePrice").asLong!!
        var upgrade = json.get("upgradeId").asString!!


        var drop = TransmitGenerators.getDataStore().GSON.fromJson(json.get("drop"), WarpItem::class.java)!!
        var block = TransmitGenerators.getDataStore().GSON.fromJson(json.get("block"), WarpItem::class.java)!!

        return Generator(id, block, drop, worth, upgrade, price)
    }
}