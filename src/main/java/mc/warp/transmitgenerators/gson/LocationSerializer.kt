package mc.warp.transmitgenerators.gson

import com.google.gson.*
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationSerializer : JsonSerializer<Location>,JsonDeserializer<Location> {

    override fun serialize(src: Location, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        var json = JsonObject()



        json.add("x", JsonPrimitive(src.x))
        json.add("y", JsonPrimitive(src.y))
        json.add("z", JsonPrimitive(src.z))
        json.add("world", JsonPrimitive(src.world.name))

        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Location {
        var json = json.asJsonObject

        var x = json.get("x").asDouble
        var y = json.get("y").asDouble
        var z = json.get("z").asDouble

        var world = json.get("world").asString

        return Location(Bukkit.getWorld(world), x, y, z)
    }


}
