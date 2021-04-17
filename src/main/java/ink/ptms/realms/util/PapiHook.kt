package ink.ptms.realms.util

import ink.ptms.realms.RealmManager
import ink.ptms.realms.Realms
import io.izzel.taboolib.module.compat.PlaceholderHook
import io.izzel.taboolib.module.inject.THook
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.math.RoundingMode
import java.text.DecimalFormat

@THook
class PapiHook : PlaceholderHook.Expansion {

    override fun plugin(): Plugin {
        return Realms.plugin
    }

    override fun identifier(): String {
        return "realms"
    }

    override fun onPlaceholderRequest(player: Player, params: String): String {
        return when (params) {
            "where" -> RealmManager.getRealmBlock(player.location)?.name ?: "null"
            else -> "null"
        }
    }
}