package ink.ptms.realms.data

import ink.ptms.realms.util.toAABB
import io.izzel.taboolib.internal.gson.JsonObject
import io.izzel.taboolib.kotlin.navigation.pathfinder.bukkit.BoundingBox
import io.izzel.taboolib.module.nms.impl.Position
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.Location
import org.bukkit.Particle

/**
 * Realms
 * ink.ptms.realms.data.RealmBlock
 *
 * @author sky
 * @since 2021/3/11 5:09 下午
 */
class RealmBlock(center: Location, var size: Int) {

    val center = center
        get() = field.clone()

    val permissions = HashMap<String, Boolean>()
    val users = HashMap<String, MutableMap<String, Boolean>>()

    val extends = HashMap<Position, Int>()
    val aabb = ArrayList<BoundingBox>()

    val node: String
        get() = "realm_${center.blockX}_${center.blockY}_${center.blockZ}"

    val json: String
        get() = JsonObject().also { json ->
            json.addProperty("size", size)
            json.add("permissions", JsonObject().also { perm ->
                permissions.forEach {
                    perm.addProperty(it.key, it.value)
                }
            })
            json.add("users", JsonObject().also { user ->
                users.forEach {
                    user.add(it.key, JsonObject().also { u ->
                        it.value.forEach { (k, v) ->
                            u.addProperty(k, v)
                        }
                    })
                }
            })
            json.add("extends", JsonObject().also { ext ->
                extends.forEach {
                    ext.addProperty("${it.key.x},${it.key.y},${it.key.z}", it.value)
                }
            })
        }.toString()

    init {
        update()
    }

    /**
     * 权限检查
     */
    fun hasPermission(key: String, player: String? = null, def: Boolean = false): Boolean {
        return if (player != null && users.containsKey(player)) {
            users[player]!![key] ?: permissions[key] ?: def
        } else {
            permissions[key] ?: def
        }
    }

    /**
     * 缓存中心及扩展的碰撞箱
     */
    fun update() {
        aabb.clear()
        aabb.add(center.toCenterLocation().toAABB(size))
        aabb.addAll(extends.map { it.key.toLocation(center.world).toCenterLocation().toAABB(it.value) })
    }

    /**x
     * 是否在领域内
     */
    fun inside(loc: Location): Boolean {
        return aabb.any { it.contains(loc.x, loc.y, loc.z) }
    }

    /**
     * 向该玩家展示领地边界
     */
    fun borderDisplay() {
        aabb.forEach { box ->
            box.buildBox().forEach { pos ->
                Effects.create(Particle.END_ROD, pos.toLocation(center.world)).count(1).range(100.0).play()
            }
        }
    }

    private fun BoundingBox.containsIn(x: Double, y: Double, z: Double): Boolean {
        return x - 1 > minX && x + 1 < maxX && y - 1 > minY && y + 1 < maxY && z - 1 > minZ && z + 1 < maxZ
    }

    private fun BoundingBox.buildBox(): List<Position> {
        val array = ArrayList<Position>()
        Effects.buildCubeStructured(Location(center.world, minX, minY, minZ), Location(center.world, maxX, maxY, maxZ), 0.2) { b ->
            if (aabb.all { it == this || !it.containsIn(b.x, b.y, b.z) }) {
                array.add(Position.at(b))
            }
        }
        return array
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RealmBlock) return false
        if (node != other.node) return false
        return true
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }

    override fun toString(): String {
        return "RealmBlock(size=$size, permissions=$permissions, users=$users, extends=$extends, node='$node')"
    }
}