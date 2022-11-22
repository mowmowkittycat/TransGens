package mc.warp.transmitgenerators

import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
import mc.warp.transmitgenerators.type.WarpPlayer
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

class API {

    companion object {

        @JvmStatic
        fun totalCostOfGen(gen: String?): Long {
            val dataStore = getDataStore()
            var generator = dataStore.getGenerator(dataStore.config.getString("starter-gen")!!)
            var price = 0L
            var done = false
            while (!done) {
                price += generator!!.price
                val newGen = dataStore.getGenerator(generator.upgrade)
                if (generator.upgrade == gen) done = true
                if (newGen != null) {

                    generator = newGen
                } else {
                    done = true
                }
            }
            return price
        }

        @JvmStatic
        fun getGeneratorList(): ArrayList<Generator> {

            return TransmitGenerators.genList
        }



        @JvmStatic
        fun getPlayerData(player: OfflinePlayer): WarpPlayer {
            return TransmitGenerators.getDataStore().getPlayer(player)!!
        }



        @JvmStatic
        fun getGeneratorGeneratorsList(player: OfflinePlayer): ArrayList<String> {
            val data = TransmitGenerators.getDataStore().getPlayer(player)!!
            val generators = ArrayList<String>();
            data.placedGens.forEach {
                for (location in it.value) {
                    generators.add(it.key);
                }

            }
            return generators
        }

        @JvmStatic
        fun getGeneratorLocationsList(player: OfflinePlayer): ArrayList<Location> {
            val data = TransmitGenerators.getDataStore().getPlayer(player)!!
            val locations = ArrayList<Location>();
            data.placedGens.forEach {
                locations.addAll(it.value);
            }

            return locations;
        }

        @JvmStatic
        fun getGeneratorLocations(player: OfflinePlayer): HashMap<String, ArrayList<Location>> {
            val data = TransmitGenerators.getDataStore().getPlayer(player)!!
            return data.placedGens;
        }

        @JvmStatic
        fun getGeneratorDrop(genType: String): ItemStack {
            val gen = getDataStore().getGenerator(genType)
            return gen!!.getDrop()
        }

        @JvmStatic
        fun getGeneratorBlock(genType: String): ItemStack {
            val gen = getDataStore().getGenerator(genType)
            return gen!!.getBlock()
        }

        @JvmStatic
        fun getGenerator(genType: String): Generator {
            val gen = getDataStore().getGenerator(genType)
            return gen!!
        }
    }


}