package ink.ptms.realms.util

import ink.ptms.realms.Realms
import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.locale.chatcolor.TColor
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.lite.cooldown.Cooldown
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

/**
 * @Author sky
 * @Since 2020-01-05 23:19
 */
interface Helper {

    fun String.toPlayer(): Player? {
        return player(this)
    }

    fun Entity.getCName(): String {
        if (this.customName != null) {
            return this.customName!!
        }
        return I18n.get().getName(this)
    }

    fun UUID.toPlayer(): Player? {
        return Bukkit.getPlayer(this)
    }

    fun String.toPapi(player: Player): String {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun List<String>.toPapi(player: Player): List<String> {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun Double.toTwo(): Double {
        return Coerce.format(this)
    }

    fun String.screen(): String {
        return this.replace("[^A-Za-z0-9\\u4e00-\\u9fa5_]".toRegex(), "")
    }

    fun String.process(): String {
        return TColor.translate(this)
            .replace("true", "§a开启§7")
            .replace("false", "§c关闭§7")
            .replace("null", "空")
    }

    fun List<String>.process(): List<String> {
        return this.map { it.process() }
    }

    fun CommandSender.done(message: String) {
        toDone(this, message)
    }

    fun CommandSender.info(message: String) {
        toInfo(this, message)
    }

    fun CommandSender.error(message: String) {
        toError(this, message)
    }

    fun heal(player: Player) {
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.fireTicks = 0
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
    }

    fun player(name: String): Player? {
        return Bukkit.getPlayerExact(name)
    }

    fun toInfo(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7${message.replace("&", "§")}")
        if (sender is Player && !cooldown.isCooldown(sender.name)) {
            sender.playSound(sender.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
        }
    }

    fun toError(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7${message.replace("&", "§")}")
        if (sender is Player && !cooldown.isCooldown(sender.name)) {
            sender.playSound(sender.location, Sound.ENTITY_VILLAGER_NO, 1f, (1..2).random().toFloat())
        }
    }

    fun toDone(sender: CommandSender, message: String) {
        sender.sendMessage("§8[§c Realms §8] §7${message.replace("&", "§")}")
        if (sender is Player && !cooldown.isCooldown(sender.name)) {
            sender.playSound(sender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, (1..2).random().toFloat())
        }
    }

    fun toConsole(message: String) {
        Bukkit.getConsoleSender().sendMessage("§8[§c Realms §8] §7${message.replace("&", "§")}")
    }

    fun run(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTask(Realms.plugin, Runnable { runnable.invoke() })
    }

    fun runAsync(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTaskAsynchronously(Realms.plugin, Runnable { runnable.invoke() })
    }

    companion object {

        @TInject
        val cooldown = Cooldown("piv:sound", 100)
    }
}