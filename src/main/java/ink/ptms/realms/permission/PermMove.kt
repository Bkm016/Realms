package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 *
 * @author 枫溪
 * @since 2021/4/18 8:30 上午
 */
@TListener
object PermMove : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "move"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.IRON_BOOTS)
            .name("&f移动 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8领域内移动"
            ).also {
                if (value) {
                    it.shiny()
                }
            }
            .flags(*ItemFlag.values())
            .colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerMoveEvent) {
        e.player.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("move", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}