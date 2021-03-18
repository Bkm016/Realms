package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermBuild : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "build"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.GRASS_BLOCK)
            .name("&f建筑 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8放置方块, 破坏方块, 放置挂饰, 破坏挂饰",
                "&8放置盔甲架, 破坏盔甲架, 装满桶, 倒空桶"
            )
            .flags(*ItemFlag.values())
            .also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: BlockBreakEvent) {
        e.block.location.getRealm()?.run {
            if (!hasPermission("admin", e.player.name) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: BlockPlaceEvent) {
        e.block.location.getRealm()?.run {
            if (!hasPermission("admin", e.player.name) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: HangingPlaceEvent) {
        val player = e.player ?: return
        e.block.location.getRealm()?.run {
            if (!hasPermission("admin", player.name) && !hasPermission("build", player.name)) {
                e.isCancelled = true
                player.warning()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: HangingBreakByEntityEvent) {
        if (e.remover is Player) {
            val player = e.remover as Player
            e.entity.location.block.location.getRealm()?.run {
                if (!hasPermission("admin", player.name) && !hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item?.type == org.bukkit.Material.ARMOR_STAND) {
            e.clickedBlock?.location?.getRealm()?.run {
                if (!hasPermission("admin", e.player.name) && !hasPermission("build", e.player.name)) {
                    e.isCancelled = true
                    e.player.warning()
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is ArmorStand) {
            val player = Servers.getAttackerInDamageEvent(e) ?: return
            e.entity.location.block.location.getRealm()?.run {
                if (!hasPermission("admin", player.name) && !hasPermission("build", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerBucketFillEvent) {
        e.block.location.getRealm()?.run {
            if (!hasPermission("admin", e.player.name) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: PlayerBucketEmptyEvent) {
        e.block.location.getRealm()?.run {
            if (!hasPermission("admin", e.player.name) && !hasPermission("build", e.player.name)) {
                e.isCancelled = true
                e.player.warning()
            }
        }
    }
}