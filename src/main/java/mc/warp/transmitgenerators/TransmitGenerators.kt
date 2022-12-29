package mc.warp.transmitgenerators

import mc.warp.transmitgenerators.commands.GenCommand
import mc.warp.transmitgenerators.commands.GenTabCommand
import mc.warp.transmitgenerators.commands.SellCommand
import mc.warp.transmitgenerators.events.GenEvents
import mc.warp.transmitgenerators.hooks.Placeholders
import mc.warp.transmitgenerators.type.WarpPlayer
import mc.warp.transmitgenerators.utils.scheduler.schedule
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandMap
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList


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

        lateinit var adventure: BukkitAudiences;
        lateinit var econ: Economy;
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
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            disableDependency()
            return;
        }

        var rsp = server.servicesManager.getRegistration( Economy::class.java )
        if (rsp == null) {
            disableDependency()
            return;
        }
        econ = rsp.provider;

        adventure = BukkitAudiences.create(this);
        server.pluginManager.registerEvents(GenEvents(), this)
        getCommand("TransmitGen")!!.setExecutor(GenCommand())
        getCommand("TransmitGen")!!.tabCompleter = GenTabCommand()

        load()



        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getScheduler().schedule(this) {
                for (p in Bukkit.getOnlinePlayers()) {
                    var player = dataStore.playerList[p.uniqueId]
                    if (player != null) {
                        player.genDrop()
                        waitFor(1)
                    }
                }
            }
        }, 0, dataStore.config.getInt("generator-cooldown").toLong() * 20)

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

    fun disableDependency() {
        this.logger.severe("\n" +
                "\n" +
                " _____                       _ _      _____                     _               \n" +
                "|_   _|___ ___ ___ ___ _____|_| |_   |   __|___ ___ ___ ___ ___| |_ ___ ___ ___ \n" +
                "  | | |  _| .'|   |_ -|     | |  _|  |  |  | -_|   | -_|  _| .'|  _| . |  _|_ -|\n" +
                "  |_| |_| |__,|_|_|___|_|_|_|_|_|    |_____|___|_|_|___|_| |__,|_| |___|_| |___|\n" +
                "\n -" +
                "\n >>> Plugin cannot be enabled due to missing dependency [ Vault ] or [ Vault Economy Manager ]" +
                "\n -")
        Bukkit.getPluginManager().disablePlugin(this);
        return
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
