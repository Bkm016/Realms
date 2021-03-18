package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermParticle : Permission {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "particle"

    override val priority: Int
        get() = -1

    override val default: Boolean
        get() = true

    override val worldSide: Boolean
        get() = true

    override val playerSide: Boolean
        get() = false

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.SEA_LANTERN)
            .name("&f边界特效 ${value.display}")
            .lore(
                "",
                "&7启用时:",
                "&8播放领域边界粒子",
            ).also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }
}