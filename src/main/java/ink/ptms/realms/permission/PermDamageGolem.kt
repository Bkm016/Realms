package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.getRealmBlock
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import ink.ptms.realms.util.warning
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.block.data.type.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermDamageGolem
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermDamageGolem : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "damage_golem"

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.STONE_SWORD)
            .name("&f攻击傀儡 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8对傀儡 (Golem) 造成伤害"
            )
            .flags(*ItemFlag.values())
            .also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.entity is Golem) {
            val player = Servers.getAttackerInDamageEvent(e) ?: return
            e.entity.location.getRealm()?.run {
                if (!hasPermission("admin", player.name) && !hasPermission("damage_golem", player.name)) {
                    e.isCancelled = true
                    player.warning()
                }
            }
        }
    }
}