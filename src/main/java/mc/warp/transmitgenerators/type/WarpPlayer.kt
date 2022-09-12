package mc.warp.transmitgenerators.type

import com.google.gson.annotations.SerializedName
import de.tr7zw.nbtapi.NBTBlock
import mc.warp.transmitgenerators.TransmitGenerators
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.ArrayList

class WarpPlayer {

    @SerializedName("UUID") var UUID: UUID;
    @SerializedName("MaxGens") var maxGenSlots: Int;
    @SerializedName("Gens") var placedGens: ArrayList<Location>;
    @Transient var canUpgrade: Boolean = true;

    constructor(player: Player) {
        this.UUID = player.uniqueId
        this.maxGenSlots = 25;
        this.placedGens = ArrayList();
    }


    fun genDrop() {
        var player = this
        var genWait = 10
        Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
            if (genWait == 0) {
                genWait = 10
                waitFor(1)
            }
            for (loc in player.placedGens) {
                var compound = NBTBlock(loc.block).data.getCompound("TransmitNBT") ?: continue
                var genID = compound.getString("generator") ?: continue
                var gen = TransmitGenerators.getDataStore().getGenerator(genID) ?: continue
                var newloc = loc.clone().add(0.5,1.0,0.5)
                loc.world.dropItem(newloc, gen.getDrop()).velocity = Vector(0.0,0.1,0.0)
            }

        }

    }

}