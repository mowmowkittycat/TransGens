package mc.warp.transmitgenerators.utils


import mc.warp.transmitgenerators.TransmitGenerators
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Duration


object Format {
    var formater = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(TagResolver.resolver("ccolor", Format::createW))
                .resolver(TagResolver.resolver("dash", Format::createDash))
                .build()
        ).build()
    var WarpColors = hashMapOf<String, String>(
        "main" to "#548EEB",
        "gray" to "#95A6BC",
        "white" to "#fafcff",
        "error" to "#7d1a1a",
        "errorlight" to "#b32b2b",
        "green" to "#56db56"
    )

    private fun createW(args: ArgumentQueue, ctx: Context): Tag {
        val clr = args.popOr("The <color> tag requires exactly one argument, the Color").value()
        return Tag.styling(TextColor.fromCSSHexString(WarpColors[clr]!!)!!)
    }

    private fun createDash(args: ArgumentQueue, ctx: Context): Tag {
        return Tag.inserting(Component.text("                            ").decorate(TextDecoration.STRIKETHROUGH))
    }

    fun sendText(player: Player, string: String) {
        sendText(player, formater.deserialize(string))
    }


    fun sendActionBar(player: Player, string: String) {
        TransmitGenerators.adventure.player(player).sendActionBar(formater.deserialize(string))
    }


    fun sendTitle(player: Player, mainTitle: String) {
        var title = Title.title(formater.deserialize(mainTitle),Component.empty());

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: String, FadeIn: Long, FadeOut: Long) {
        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(1000), Duration.ofMillis(FadeOut))
        var title = Title.title(formater.deserialize(mainTitle),Component.empty(), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: String, FadeIn: Long, FadeOut: Long, duration: Long) {
        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(duration), Duration.ofMillis(FadeOut))
        var title = Title.title(formater.deserialize(mainTitle),Component.empty(), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }



    fun sendTitle(player: Player, mainTitle: String, subTitle: String) {
        var title = Title.title(formater.deserialize(mainTitle),formater.deserialize(subTitle));

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: String, subTitle: String, FadeIn: Long, FadeOut: Long) {
        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(1000), Duration.ofMillis(FadeOut))
        var title = Title.title(formater.deserialize(mainTitle),formater.deserialize(subTitle), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: String,subTitle: String, FadeIn: Long, FadeOut: Long, duration: Long) {
        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(duration), Duration.ofMillis(FadeOut))
        var title = Title.title(formater.deserialize(mainTitle),formater.deserialize(subTitle), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }

    fun formatValue(value: Double): String {
        var value = value
        val power: Int
        val suffix = " kmbtq"
        var formattedNumber = ""
        val formatter: NumberFormat = DecimalFormat("#,###.#")
        power = StrictMath.log10(value).toInt()
        value = value / Math.pow(10.0, (power / 3 * 3).toDouble())
        formattedNumber = formatter.format(value)
        formattedNumber = formattedNumber + suffix[power / 3]
        return if (formattedNumber.length > 4) formattedNumber.replace("\\.[0-9]+".toRegex(), "") else formattedNumber
    }


    fun sendText(player: Player, string: Component?) {
        if (string == null) return

        TransmitGenerators.adventure.player(player).sendMessage(string)
    }
    fun sendText(player: CommandSender, string: Component?) {
        if (string == null) return
        if (player is Player) {
            TransmitGenerators.adventure.player(player).sendMessage(string)
        } else{
            TransmitGenerators.adventure.console().sendMessage(string)
        }
    }
    fun sendActionBar(player: Player, string: Component?) {
        if (string == null) return

        TransmitGenerators.adventure.player(player).sendActionBar(string)
    }


    fun sendTitle(player: Player, mainTitle: Component?) {
        if (mainTitle == null) return

        var title = Title.title(mainTitle,Component.empty());

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: Component?, FadeIn: Long, FadeOut: Long) {
        if (mainTitle == null) return

        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(1000), Duration.ofMillis(FadeOut))
        var title = Title.title(mainTitle,Component.empty(), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: Component?, FadeIn: Long, FadeOut: Long, duration: Long) {
        if (mainTitle == null) return

        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(duration), Duration.ofMillis(FadeOut))
        var title = Title.title(mainTitle,Component.empty(), times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }



    fun sendTitle(player: Player, mainTitle: Component?, subTitle: Component?) {
        var subTitle = subTitle
        var mainTitle = mainTitle
        if (mainTitle == null) mainTitle = Component.empty()
        if (subTitle == null) subTitle = Component.empty()

        var title = Title.title(mainTitle,subTitle);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: Component?, subTitle: Component?, FadeIn: Long, FadeOut: Long) {
        var subTitle = subTitle
        var mainTitle = mainTitle
        if (mainTitle == null) mainTitle = Component.empty()
        if (subTitle == null) subTitle = Component.empty()

        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(1000), Duration.ofMillis(FadeOut))
        var title = Title.title(mainTitle,subTitle, times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }
    fun sendTitle(player: Player, mainTitle: Component?,subTitle: Component?, FadeIn: Long, FadeOut: Long, duration: Long) {
        var subTitle = subTitle
        var mainTitle = mainTitle
        if (mainTitle == null) mainTitle = Component.empty()
        if (subTitle == null) subTitle = Component.empty()

        val times = Title.Times.times(Duration.ofMillis(FadeIn), Duration.ofMillis(duration), Duration.ofMillis(FadeOut))
        var title = Title.title(mainTitle,subTitle, times);

        TransmitGenerators.adventure.player(player).showTitle(title)
    }


    fun text(string: String): Component {
        return formater.deserialize(string)
    }

}
