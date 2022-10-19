package mc.warp.transmitgenerators

import mc.warp.transmitgenerators.TransmitGenerators.Companion.getDataStore
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
            TransmitGenerators.genList.size

            return TransmitGenerators.genList
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