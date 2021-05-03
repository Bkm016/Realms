package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.getRealmBlock
import ink.ptms.realms.RealmManager.isAdmin
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.block.data.type.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermContainer
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
@TListener
object PermContainer : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "container"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.CHEST)
            .name("&f容器 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8打开容器"
            )
            .flags(*ItemFlag.values())
            .also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: InventoryOpenEvent) {
        if (e.inventory.location != null) {
            e.inventory.location?.getRealm()?.run {
                val player = e.player as? Player ?: return
                if (!isAdmin(player) && !hasPermission("container", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }
}