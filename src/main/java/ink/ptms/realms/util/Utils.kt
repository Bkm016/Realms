package ink.ptms.realms.util

import io.izzel.taboolib.kotlin.navigation.pathfinder.bukkit.BoundingBox
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.Location
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

fun Location.toAABB(size: Int) = BoundingBox(
    x - size - 0.5,
    y - size - 0.5,
    z - size - 0.5,
    x + size + 1.5,
    y + size + 1.5,
    z + size + 1.5
)

fun BoundingBox.getVertex(): List<Vector> {
    return listOf(
        Vector(minX, minY, minZ),
        Vector(maxY, minY, minZ),
        Vector(maxY, minY, maxY),
        Vector(minX, minY, maxZ),
        Vector(minX, maxY, minZ),
        Vector(maxY, maxY, minZ),
        Vector(maxY, maxY, maxY),
        Vector(minX, maxY, maxZ),
    )
}

val Boolean.display: String
    get() = if (this) "§a允许" else "§c阻止"

fun HumanEntity.warning() {
    TLocale.Display.sendActionBar(this as Player, "§c§l:(§7 当前行为受所属领域保护.")
}