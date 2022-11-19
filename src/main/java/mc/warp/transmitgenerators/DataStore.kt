package mc.warp.transmitgenerators

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import mc.warp.transmitgenerators.gson.GeneratorSerializer
import mc.warp.transmitgenerators.gson.ItemStackSerializer
import mc.warp.transmitgenerators.gson.LocationSerializer
import mc.warp.transmitgenerators.type.WarpPlayer
import mc.warp.transmitgenerators.type.WarpSound
import org.bson.Document
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors


class DataStore() {

    var playerList = HashMap<UUID, WarpPlayer>()

    var sounds = HashMap<String, WarpSound>()
    lateinit var messages: YamlConfiguration;
    lateinit var config: YamlConfiguration;
    private var genList = HashMap<String, Generator>()


    private var dataFolder: File = TransmitGenerators.getInstance().dataFolder;
    private var soundFile: File = File(dataFolder.absolutePath + "/sounds.json");
    private var genFile: File = File(dataFolder.absolutePath + "/gens.json");
    private var configFile: File = File(dataFolder.absolutePath + "/config.yml");
    private var playerFolder: File = File(dataFolder.absolutePath + "/players")
    private var langFile: File = File(dataFolder.absolutePath + "/lang.yml");
    private var mongo: MongoClient? = null;
    private var database: MongoDatabase? = null;
    private var collection: MongoCollection<Document>? = null;
    var GSON: Gson;

    init {
        GSON = GsonBuilder()
            .registerTypeAdapter(Location::class.java, LocationSerializer())
            .registerTypeAdapter(ItemStack::class.java, ItemStackSerializer())
            .registerTypeAdapter(Generator::class.java, GeneratorSerializer())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
    }




    fun Initialization() {
        dataFolder.mkdir()

        if (!configFile.exists()) {
            configFile.createNewFile()
            copyResourceFile("config.yml", configFile)
        }
        loadConfig()






        var guiFolder = File(dataFolder.absolutePath + "/guis")
        if (!guiFolder.exists()) {
            guiFolder.mkdir()
            copyResourceFile("guis/genList.xml", File(guiFolder.absolutePath + "/genList.xml"))

        }
        if (!langFile.exists()) {
            langFile.createNewFile()
            copyResourceFile("lang.yml", langFile)
        }
        loadLang()
        if (!soundFile.exists()) {
            soundFile.createNewFile()
            copyResourceFile("sounds.json", soundFile)
        }
        loadSound()

        if (!genFile.exists()) {
            genFile.createNewFile()
            Generator("dirt", ItemStack(Material.DIRT), ItemStack(Material.BROWN_DYE), 1, "stone", 50);
            Generator("stone", ItemStack(Material.STONE), ItemStack(Material.STONE_BUTTON), 2, "coal", 100);
            saveGenerators()
        } else {
            loadGenerators()
        }

        var databaseConfig = config.getConfigurationSection("database")!!
        var databaseType = databaseConfig.getString("type")
        if (databaseType.equals("default",true)) {

            if (!playerFolder.exists()) {
                playerFolder.mkdir()
            }
        } else if (databaseType.equals("mongodb", true)) {
            if (databaseConfig.getString("database-uri") == null) {
                TransmitGenerators.getInstance().logger.severe("Database URI malformed.")
            }
            mongo = MongoClients.create(databaseConfig.getString("database-uri"))
            database = mongo!!.getDatabase(databaseConfig.getString("database-name"));
            collection = database!!.getCollection(databaseConfig.getString("collection-name"));


        }
    }

    private fun loadGenerators() {
        if (!genFile.exists()) throw NullPointerException("Generator Save File is null")

        var Reader = FileReader(genFile)
        var type = object : TypeToken<ArrayList<Generator>>() {} .type;
        var result = GSON.fromJson<ArrayList<Generator>>(Reader, type)
        for (gen in result) {
            this.setGenerator(gen.id, gen)
        }

    }

    private fun saveGenerators() {

        if (!genFile.exists()) {
            genFile.createNewFile()
        }

        val writer: Writer = FileWriter(genFile, false)
        val genList = ArrayList<Generator>()
        for (gen in this.getAllGenerators()) {
            TransmitGenerators.getInstance().logger.info("Generator: (${gen.value.id}) has been saved")
            genList.add(gen.value);
        }

        GSON.toJson(genList, writer)
        writer.flush()
        writer.close()
    }

    fun loadPlayer(player: OfflinePlayer): WarpPlayer? {
        var databaseType = this.config.getConfigurationSection("database")!!.getString("type")
        if (databaseType.equals("default", true)) {
            var saveFile = File(playerFolder.absolutePath + "/${player.uniqueId}.json")


            if (!saveFile.exists() && player is Player) {
                saveFile.createNewFile()



                return WarpPlayer(player)
            }

            var Reader = FileReader(saveFile)
            var type = object : TypeToken<WarpPlayer>() {} .type
            return GSON.fromJson<WarpPlayer>(Reader, type)
        } else if (databaseType.equals("mongodb", true)) {

            var json = collection!!.find(eq("_id", player.uniqueId.toString())).first()

            if (json == null) {
                collection!!.insertOne(Document().append("_id",player.uniqueId.toString()))
                return WarpPlayer(player as Player)
            }
            var type = object : TypeToken<WarpPlayer>() {} .type
            var result = GSON.fromJson<WarpPlayer>(json.toJson(), type)
            result.UUID = player.uniqueId
            return result
        }
        return null;
    }
    fun deletePlayerFile(player: OfflinePlayer) {
        var saveFile = File(playerFolder.absolutePath + "/${player.uniqueId}.json")
        saveFile.delete()
    }


    fun firstPlayerLoad(player: Player) {
        var playerData = TransmitGenerators.getDataStore().loadPlayer(player)
        TransmitGenerators.getDataStore().setPlayer(player, playerData!!);
        playerData.canUpgrade = true
    }

    fun saveWarpPlayer(player: WarpPlayer) {
        var databaseType = this.config.getConfigurationSection("database")!!.getString("type")
        if (databaseType.equals("default", true)) {
            var saveFile = File(playerFolder.absolutePath + "/${player.UUID}.json")

            if (!saveFile.exists()) {
                saveFile.createNewFile()
            }
            var writer: Writer = FileWriter(saveFile, false)

            GSON.toJson(player, writer)
            writer.flush()
            writer.close()
        } else if (databaseType.equals("mongodb", true)) {

            var json = GSON.toJsonTree(player)
            TransmitGenerators.getInstance().logger.info(json.toString())
            collection!!.replaceOne(eq("_id", player.UUID.toString()), Document.parse(json.toString()))
        }
    }

    fun savePlayer(player: OfflinePlayer) {
        var playerData: WarpPlayer = this.getPlayer(player) ?: return
        saveWarpPlayer(playerData)
    }


    private fun copyResourceFile(resource: String, file: File) {
        var resourceFile = TransmitGenerators.getInstance().getResource(resource)
        var resourceString = BufferedReader(
            InputStreamReader(resourceFile, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"))
        val writer: Writer = FileWriter(file, false)
        writer.write(resourceString)
        writer.flush()
        writer.close()
    }


    private fun loadLang() {
        if (!langFile.exists()) throw NullPointerException("Lang Save File is null")

        messages = YamlConfiguration.loadConfiguration(langFile)
    }
    private fun loadConfig() {

        if (!configFile.exists()) throw NullPointerException("Config file does not exist")

        config = YamlConfiguration.loadConfiguration(configFile)

    }


    private fun loadSound() {
        if (!soundFile.exists()) throw NullPointerException("Sound Save File is null")

        var Reader = FileReader(soundFile)
        var type = object : TypeToken<HashMap<String, WarpSound>>() {} .type;
        var result = GSON.fromJson<HashMap<String, WarpSound>>(Reader, type)
        sounds = result
    }


    fun getGenerator(gen: String): Generator? {
        return genList[gen]
    }

    fun getAllGenerators(): HashMap<String, Generator> {
        return genList
    }

    fun setAllGenerators(gens: HashMap<String, Generator>) {
        genList = gens
    }

    fun setGenerator(ID: String, generator: Generator ) {
        genList[ID] = generator
    }

    fun getPlayer(player: OfflinePlayer): WarpPlayer? {
        return playerList[player.uniqueId]
    }

    fun setPlayer(player: Player, WarpPlayer: WarpPlayer) {
        playerList[player.uniqueId] = WarpPlayer
    }





}