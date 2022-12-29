package mc.warp.transmitgenerators.type

import com.google.gson.annotations.SerializedName
import de.tr7zw.nbtapi.NBTBlock
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.TransmitGenerators.Companion.genBlockList
import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WarpPlayer {

    @SerializedName("UUID") var UUID: UUID;
    @SerializedName("MaxGens") var maxGenSlots: Int;
    @SerializedName("PlacedGens") var placedGenSlots: Int;
    @SerializedName("Gens") var placedGens: HashMap<String, ArrayList<Location>>
    @Transient var canUpgrade: Boolean = true;
    @Transient var ephemeralBlocks: HashMap<Location, Boolean>;

    constructor(player: Player) {
        this.UUID = player.uniqueId
        this.maxGenSlots = getDataStore().config.getInt("default-genslots")
        this.placedGenSlots = 0
        this.placedGens = HashMap()
        this.ephemeralBlocks = HashMap();

    }


    fun genDrop() {
        var player = this
        var genWait = 10
        if (player.placedGenSlots < 0) player.placedGenSlots = 0;
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            var copy = player.placedGens.clone() as HashMap<String, ArrayList<Location>>
            var remove = HashMap<String, ArrayList<Location>>()
            for (type in copy) {
                var gen = getDataStore().getGenerator(type.key) ?: continue
                var genDrop = gen.getDrop()
                val genBlock = gen.getBlock()

                val locs = type.value.clone() as ArrayList<Location>

                for (loc in locs) {
                    if (!loc.isChunkLoaded) continue
                    if (genWait == 0) {
                        genWait = 10
                        waitFor(1)
                    }
                    var nbt = NBTBlock(loc.block)
                    if (loc.block.type != genBlock.type) {
                        if (!genBlockList.contains(loc.block.type)) {
                            if (player.ephemeralBlocks != null) {
                                if (!player.ephemeralBlocks.containsKey(loc)) {
                                    remove.getOrDefault(type.key, ArrayList()).add(loc)
                                    placedGenSlots--
                                }
                            }



                        }
                    }

                    var newloc = loc.clone().add(0.5,1.0,0.5)
                    loc.world.dropItem(newloc, genDrop).velocity = Vector(0.0,0.1,0.0)
                    genWait -= TransmitGenerators.genWait

                }
            }

            for (type in remove) {
                var array = player.placedGens.getOrDefault(type.key, ArrayList())
                array.removeAll(type.value.toSet())
            }

            player.placedGens = copy;
            player.ephemeralBlocks = HashMap();
        }

    }

}