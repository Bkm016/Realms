package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.getRealm
import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.entity.Animals
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 *
 * @author 枫溪
 * @since 2021/4/18 8:30 上午
 */
@TListener
object PermAnimalsSpawn : Permission, Listener {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "animals_spawn"

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.SHEEP_SPAWN_EGG)
            .name("&f动物产生 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8生成动物"
            ).also {
                if (value) {
                    it.shiny()
                }
            }
            .flags(*ItemFlag.values())
            .colored().build()
    }

    @EventHandler(ignoreCancelled = true)
    fun e(e: EntitySpawnEvent) {
        if (e.entity !is Animals){
            return
        }
        e.entity.location.getRealm()?.run {
            if (!hasPermission("animals_spawn", def = false)) {
                e.isCancelled = true
            }
        }
    }
}