package mc.warp.transmitgenerators

import com.idlegens.idlecore.IdleCore
import com.idlegens.idlecore.stats.Stat
import com.idlegens.idlecore.stats.StatManager.getStats
import mc.warp.transmitgenerators.commands.GenCommand
import mc.warp.transmitgenerators.commands.GenTabCommand
import mc.warp.transmitgenerators.commands.SellCommand
import mc.warp.transmitgenerators.events.GenEvents
import mc.warp.transmitgenerators.hooks.Placeholders
import mc.warp.transmitgenerators.type.WarpPlayer
import mc.warp.transmitgenerators.utils.scheduler.schedule
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandMap
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil


class TransmitGenerators : JavaPlugin() {

    companion object {
        private lateinit var instance: TransmitGenerators;
        private lateinit var dataStore: DataStore;
        fun getInstance(): TransmitGenerators {
            return instance;
        }
        fun getDataStore() : DataStore {
            return dataStore
        }

        var genWait: Int = 0;
        lateinit var genList: ArrayList<Generator>
        var genBlockList: ArrayList<Material> = ArrayList()
    }




    override fun onEnable() {
        instance = this



        var startTime = System.currentTimeMillis()
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholders(this).register();
        }

        server.pluginManager.registerEvents(GenEvents, this)
        getCommand("TransmitGen")!!.setExecutor(GenCommand())
        getCommand("TransmitGen")!!.tabCompleter = GenTabCommand()

        load()



        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getScheduler().schedule(this) {
                for (p in Bukkit.getOnlinePlayers()) {
                    var player = dataStore.playerList[p.uniqueId]
                    if (player != null) {
                        if (player.genTicks <= 0) {
                            player.genDrop()
                            var baseSpeed = dataStore.config.getInt("generator-cooldown") * 20
                            var genSpeed = p.getStats().currentStats[Stat.GENERATOR_SPEED] ?: 0.0
                            if (genSpeed == 0.0) player.genTicks = baseSpeed;
                            else player.genTicks = ceil(baseSpeed.toDouble() * ( 100 / genSpeed)).toInt()
                            if (player.genTicks < 1) player.genTicks = 1
                            waitFor(1)
                        }
                        player.genTicks = player.genTicks - 1;
                    }
                }
            }
        }, 0, 1)


        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getScheduler().schedule(this) {
                for (player in Bukkit.getOnlinePlayers()) {
                    dataStore.savePlayer(player)
                }
            }
        }, 0, (dataStore.config.getInt("autosave-frequency").toLong() * 20) * 60)

        var endTime = System.currentTimeMillis()

        var diffTime = endTime - startTime

        this.logger.info("\n" +
                "\n" +
                " _____                       _ _      _____                     _               \n" +
                "|_   _|___ ___ ___ ___ _____|_| |_   |   __|___ ___ ___ ___ ___| |_ ___ ___ ___ \n" +
                "  | | |  _| .'|   |_ -|     | |  _|  |  |  | -_|   | -_|  _| .'|  _| . |  _|_ -|\n" +
                "  |_| |_| |__,|_|_|___|_|_|_|_|_|    |_____|___|_|_|___|_| |__,|_| |___|_| |___|\n" +
                "\n -" +
                "\n >>> Plugin has successfully loaded in $diffTime ms" +
                "\n -")



    }


    fun load(first: Boolean=false) {
        dataStore = DataStore();
        dataStore.Initialization()
        if (!first) {
            for (player in Bukkit.getOnlinePlayers()) {
                dataStore.firstPlayerLoad(player);
            }
        }

        if (dataStore.config.getBoolean("generator-waitPer10")) {
            genWait = 1
        }
        var generatorList = dataStore.getAllGenerators().values.toMutableList() as ArrayList
        generatorList.sortBy {
            it.worth
        }

        genList = generatorList
        genList.forEach {
            genBlockList.add(it.getBlock().type)
        }



        var sellEnabled = dataStore.config.getBoolean("sell-command")
        if (sellEnabled) {
            try {
                val bukkitCommandMap: Field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
                bukkitCommandMap.isAccessible = true
                var commandMap = bukkitCommandMap.get(Bukkit.getServer()) as CommandMap
                commandMap.register("TransmitGenerators", SellCommand())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }
    fun unload() {

        for (player in Bukkit.getOnlinePlayers()) {
            dataStore.savePlayer(player)
        }
    }


    override fun onDisable() {

        var startTime = System.currentTimeMillis()

        unload()

        var endTime = System.currentTimeMillis()

        var diffTime = endTime - startTime

        this.logger.info("Generators took $diffTime ms to save")

    }


    fun genLoop() {
        for (entry in dataStore.playerList) {
            var player = entry.value
            player.genDrop()
        }
    }

}
