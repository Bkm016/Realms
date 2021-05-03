package ink.ptms.realms.permission

import org.bukkit.inventory.ItemStack

/**
 * Realms
 * ink.ptms.realms.permission.Permission
 *
 * @author sky
 * @since 2021/3/18 9:20 上午
 */
interface Permission {

    /**
     * 界面优先级
     */
    val priority: Int
        get() = 0

    /**
     * 默认选项
     */
    val default: Boolean
        get() = false

    /**
     * 序号
     */
    val id: String

    /**
     * 世界权限
     */
    val worldSide: Boolean

    /**
     * 玩家权限
     */
    val playerSide: Boolean

    /**
     * 管理员可视
     */
    val adminSide: Boolean
        get() = false

    /**
     * 构建界面物品
     */
    fun generateMenuItem(value: Boolean): ItemStack
}