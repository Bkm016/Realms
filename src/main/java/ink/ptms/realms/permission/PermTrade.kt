package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.getRealmBlock
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.block.data.type.*
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermInteract
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermTrade : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "trade"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.EMERALD)
            .name("&f交易 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8村民交易"
            ).also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerInteractAtEntityEvent) {
        if (e.rightClicked is Villager) {
            e.rightClicked.location.getRealm()?.run {
                if (!hasPermission("admin", e.player.name) && !hasPermission("trade", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }
}