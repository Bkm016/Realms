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
import org.bukkit.block.data.type.Bed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermInteract
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
@TListener
object PermBed : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "bed"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.BLUE_BED)
            .name("&f睡觉 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8使用床"
            ).also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock != null && e.clickedBlock!! is Bed) {
            e.clickedBlock?.location?.getRealm()?.run {
                if (!isAdmin(e.player) && !hasPermission("interact", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }
}