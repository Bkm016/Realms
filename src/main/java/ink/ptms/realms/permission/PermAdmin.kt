package ink.ptms.realms.permission

import ink.ptms.realms.RealmManager.register
import ink.ptms.realms.util.display
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.PermAdmin
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
object PermAdmin : Permission {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "admin"

    override val priority: Int
        get() = -1

    override val worldSide: Boolean
        get() = false

    override val playerSide: Boolean
        get() = true

    override fun generateMenuItem(value: Boolean): ItemStack {
        return ItemBuilder(XMaterial.COMMAND_BLOCK)
            .name("&f最高权力 ${value.display}")
            .lore(
                "",
                "&7允许行为:",
                "&8破坏领域, 扩展领域, 管理领域",
                "",
                "&4注意!",
                "&c对方将获得你的所有权力"
            )
            .flags(*ItemFlag.values())
            .also {
                if (value) {
                    it.shiny()
                }
            }.colored().build()
    }
}