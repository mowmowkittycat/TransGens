package mc.warp.transmitgenerators

import mc.warp.transmitgenerators.commands.GenCommand
import mc.warp.transmitgenerators.commands.GenTabCommand
import mc.warp.transmitgenerators.commands.SellCommand
import mc.warp.transmitgenerators.events.GenEvents
import mc.warp.transmitgenerators.hooks.Placeholders
import mc.warp.transmitgenerators.utils.scheduler.schedule
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field


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
    }




    override fun onEnable() {
        instance = this



        var startTime = System.currentTimeMillis()
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Placeholders(this).register();
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            disableDependency("Vault")
            return;
        }

        var rsp = server.servicesManager.getRegistration( Economy::class.java )
        if (rsp == null) {
            disableDependency("Vault")
            return;
        }
        econ = rsp.provider;

        adventure = BukkitAudiences.create(this);
        server.pluginManager.registerEvents(GenEvents(), this)
        getCommand("TransmitGen")!!.setExecutor(GenCommand())
        getCommand("TransmitGen")!!.tabCompleter = GenTabCommand()

        load()



        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getScheduler().schedule(TransmitGenerators.getInstance()) {
                for (entry in dataStore.playerList) {
                    var player = entry.value
                    player.genDrop()
                    waitFor(1)

                }
            }
        }, 0, 100)

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
        dataStore.DataInitialization()
        if (first == false) {
            for (player in Bukkit.getOnlinePlayers()) {
                dataStore.firstPlayerLoad(player);
            }
        }
        var sellEnabled = dataStore.config.getOrElse("sell_command_enabled") { false } as Boolean
        if (sellEnabled) {
            try {
                val bukkitCommandMap: Field = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
                bukkitCommandMap.setAccessible(true)
                var commandMap = bukkitCommandMap.get(Bukkit.getServer()) as CommandMap
                commandMap.register("sell", SellCommand())
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

    fun disableDependency(string: String) {
        this.logger.severe("\n" +
                "\n" +
                " _____                       _ _      _____                     _               \n" +
                "|_   _|___ ___ ___ ___ _____|_| |_   |   __|___ ___ ___ ___ ___| |_ ___ ___ ___ \n" +
                "  | | |  _| .'|   |_ -|     | |  _|  |  |  | -_|   | -_|  _| .'|  _| . |  _|_ -|\n" +
                "  |_| |_| |__,|_|_|___|_|_|_|_|_|    |_____|___|_|_|___|_| |__,|_| |___|_| |___|\n" +
                "\n -" +
                "\n >>> Plugin cannot be enabled due to missing dependency [ Vault ]" +
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