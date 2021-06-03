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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermInteract
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
@TListener
object PermItem : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "item"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.APPLE)
            .name("&f物品 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8物品丢弃, 物品捡起"
            ).also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerDropItemEvent) {
        e.player.location.getRealm()?.run {
            if (!isAdmin(e.player) && !hasPermission("item", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: EntityPickupItemEvent) {
        if (e.entity is Player) {
            e.entity.location.getRealm()?.run {
                if (!isAdmin(e.entity as Player) && !hasPermission("item", e.entity.name)) {
                    e.isCancelled = true
                    (e.entity as Player).warning()
                }
            }
        }
    }
}