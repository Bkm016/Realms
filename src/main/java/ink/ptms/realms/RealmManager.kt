package ink.ptms.realms

import ink.ptms.realms.data.RealmBlock
import ink.ptms.realms.data.RealmWorld
import ink.ptms.realms.event.RealmsJoinEvent
import ink.ptms.realms.event.RealmsLeaveEvent
import ink.ptms.realms.permission.Permission
import ink.ptms.realms.util.Helper
import ink.ptms.realms.util.getVertex
import ink.ptms.realms.util.toAABB
import io.izzel.taboolib.internal.gson.JsonParser
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.kotlin.Tasks
import io.izzel.taboolib.kotlin.blockdb.BlockFactory.createDataContainer
import io.izzel.taboolib.kotlin.blockdb.Data
import io.izzel.taboolib.kotlin.blockdb.event.BlockDataDeleteEvent
import io.izzel.taboolib.kotlin.sendHolographic
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.nms.NMS
import io.izzel.taboolib.module.nms.impl.Position
import io.izzel.taboolib.module.nms.nbt.NBTBase
import io.izzel.taboolib.util.Baffle
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.ClickEvent
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import io.izzel.taboolib.util.item.inventory.linked.MenuLinked
import io.izzel.taboolib.util.lite.Effects
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentHashMap

/**
 * Realms
 * ink.ptms.realms.RealmManager
 *
 * @author sky
 * @since 2021/3/11 5:09 ??????
 */
@TListener
object RealmManager : Listener, Helper {

    private val permissions = ArrayList<Permission>()
    private val worlds = ConcurrentHashMap<String, RealmWorld>()
    private val baffle = Baffle.of(10)

    @TSchedule
    private fun init() {
        Bukkit.getWorlds().forEach {
            val realmWorld = it.realmWorld()
            it.loadedChunks.forEach { chunk ->
                realmWorld.realms[chunk.chunkKey] = chunk.realms().toMutableList()
            }
        }
    }

    @TSchedule(period = 2, async = true)
    private fun particle() {
        Bukkit.getWorlds().forEach {
            it.realms().forEach { realm ->
                Effects.create(Particle.DOLPHIN, realm.center.toCenterLocation())
                    .offset(doubleArrayOf(0.5, 0.5, 0.5))
                    .count(5)
                    .range(100.0)
                    .play()
                realm.extends.forEach { (pos, _) ->
                    Effects.create(Particle.REDSTONE, pos.toLocation(realm.center.world).toCenterLocation())
                        .offset(doubleArrayOf(0.5, 0.5, 0.5))
                        .count(5)
                        .range(100.0)
                        .data(Effects.ColorData(Color.fromRGB(152, 249, 255), 1f))
                        .play()
                    Effects.buildLine(
                        realm.center.toCenterLocation(),
                        pos.toLocation(realm.center.world).toCenterLocation(),
                        { loc ->
                            Effects.create(Particle.REDSTONE, loc)
                                .count(1)
                                .range(100.0)
                                .data(Effects.ColorData(Color.fromRGB(152, 249, 255), 1f))
                                .play()
                        },
                        0.35
                    )
                }
                if (realm.hasPermission("particle", def = true) && baffle.hasNext()) {
                    realm.borderDisplay()
                }
            }
        }
    }

    @EventHandler
    private fun e(e: ChunkLoadEvent) {
        val realmWorld = e.chunk.world.realmWorld()
        if (realmWorld.realms.containsKey(e.chunk.chunkKey)) {
            return
        }
        realmWorld.realms[e.chunk.chunkKey] = e.chunk.realms().toMutableList()
    }

    @EventHandler
    private fun e(e: ChunkUnloadEvent) {
        val realmWorld = e.chunk.world.realmWorld()
        if (realmWorld.realms.containsKey(e.chunk.chunkKey)) {
            realmWorld.realms.remove(e.chunk.chunkKey)
        }
    }

    @EventHandler
    private fun e(e: BlockDataDeleteEvent) {
        if (e.reason != BlockDataDeleteEvent.Reason.BREAK) {
            e.isCancelled = true
        }
    }

    /**
     * ??????????????????
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: BlockPistonExtendEvent) {
        e.check(e.blocks)
    }

    /**
     * ??????????????????
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: BlockPistonRetractEvent) {
        e.check(e.blocks)
    }

    private fun BlockPistonEvent.check(blocks: List<Block>) {
        for (block in blocks) {
            if (block.isRealmBlock()) {
                isCancelled = true
                return
            }
        }
    }

    @EventHandler
    private fun e(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock!!.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.clickedBlock!!.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name)) {
                realmBlock.open(e.player)
            } else {
                e.player.sendHolographic(e.clickedBlock!!.location.add(0.5, 1.0, 0.5), "&c:(", "&7???????????????.")
            }
        }
    }

    @EventHandler
    private fun e(e: BlockBreakEvent) {
        if (e.block.isRealmBlock()) {
            e.isCancelled = true
            val realmBlock = e.block.getRealmBlock()!!
            if (realmBlock.hasPermission("admin", e.player.name) || e.player.isOp) {
                // ????????????
                if (realmBlock.center == e.block.location) {
                    // ????????????
                    if (realmBlock.extends.isNotEmpty()) {
                        e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "&c:(", "&7??????????????????????????????.")
                    } else {
                        e.player.sendHolographic(e.block.location.toCenterLocation(), "&c:(", "&7???????????????.")
                        e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                        e.block.type = Material.AIR
                        e.block.world.dropItem(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                            it.amount = realmBlock.size
                        })
                        realmBlock.remove()
                    }
                }
                // ????????????
                else if (realmBlock.extends.containsKey(Position.at(e.block.location))) {
                    e.block.world.playEffect(e.block.location.toCenterLocation(), Effect.STEP_SOUND, e.block.type)
                    e.block.type = Material.AIR
                    e.block.world.dropItem(e.block.location.toCenterLocation(), Realms.realmsDust.also {
                        it.amount = realmBlock.extends.remove(Position.at(e.block.location))!!
                    })
                    e.player.sendHolographic(e.block.location.toCenterLocation(), "&c:(", "&7???????????????.")
                    realmBlock.update()
                    realmBlock.save()
                }
            } else {
                e.player.sendHolographic(e.block.location.add(0.5, 1.0, 0.5), "&c:(", "&7???????????????.")
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun e(e: BlockPlaceEvent) {
        val realmSize = e.itemInHand.getRealmSize()
        if (realmSize > 0) {
            val vertex = e.block.location.toCenterLocation().toAABB(realmSize).getVertex().mapNotNull { vertex ->
                vertex.toLocation(e.block.world).getRealm()
            }
            when {
                vertex.isEmpty() -> {
                    RealmBlock(e.block.location, realmSize).create(e.player)
                    e.block.createDataContainer()["realms"] = Data(true)
                    e.block.blockData = e.block.blockData.also {
                        if (it is Waterlogged) {
                            it.isWaterlogged = true
                        }
                    }
                    e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                    e.player.sendHolographic(e.block.location.toCenterLocation().add(0.0, 1.0, 0.0), "&e:)", "&f???????????????")
                }
                vertex.any { !it.hasPermission("admin", e.player.name) } -> {
                    e.isCancelled = true
                    e.player.sendHolographic(e.block.location.toCenterLocation(), "&4:(", "&7?????????????????????????????????.")
                }
                else -> {
                    val pt = e.blockPlaced.type
                    val pd = e.blockPlaced.blockData.clone()
                    e.isCancelled = true
                    e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                    // ??????????????????
                    object : MenuLinked<RealmBlock>(e.player) {

                        init {
                            addButtonPreviousPage(47)
                            addButtonNextPage(51)
                        }

                        override fun getTitle() = "????????????"

                        override fun getRows() = 6

                        override fun getElements() = vertex.toSet().toList()

                        override fun getSlots() = Items.INVENTORY_CENTER.toList()

                        override fun onBuild(inventory: Inventory) {
                            if (hasPreviousPage()) {
                                inventory.setItem(47, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                            } else {
                                inventory.setItem(47, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                            }
                            if (hasNextPage()) {
                                inventory.setItem(51, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                            } else {
                                inventory.setItem(51, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                            }
                            inventory.setItem(
                                49, ItemBuilder(XMaterial.OAK_SIGN)
                                    .name("&f????????????")
                                    .lore(
                                        "&7????????????????????????????????????????????????",
                                        "&7????????????????????????????????????????????????",
                                        "",
                                        "&4??????!",
                                        "&c????????????????????????????????????????????????",
                                        "&c????????????????????????????????????????????????"
                                    ).colored().build()
                            )
                        }

                        override fun onClick(event: ClickEvent, element: RealmBlock) {
                            val verify =
                                e.block.location.toCenterLocation().toAABB(realmSize).getVertex().mapNotNull { vertex ->
                                    vertex.toLocation(e.block.world).getRealm()
                                }
                            // ????????????????????????
                            if (verify.any { !it.hasPermission("admin", e.player.name) }) {
                                e.player.closeInventory()
                                e.player.sendHolographic(e.block.location.toCenterLocation(), "&4:(", "&7?????????????????????????????????.")
                                return
                            }
                            // ??????????????????
                            val select = verify.firstOrNull { it == element }
                            if (select == null) {
                                e.player.closeInventory()
                                e.player.sendHolographic(e.block.location.toCenterLocation(), "&4:(", "&7????????????.")
                                return
                            }
                            // ??????????????????
                            select.extends[Position.at(e.block.location)] = realmSize
                            // ??????????????????
                            verify.forEach {
                                if (it != select) {
                                    it.remove()
                                    select.extends[Position.at(it.center)] = it.size
                                    select.extends.putAll(it.extends)
                                }
                            }
                            select.save()
                            select.update()
                            Tasks.task(true) {
                                select.borderDisplay()
                            }
                            if (e.player.gameMode == GameMode.SURVIVAL) {
                                e.itemInHand.amount--
                            }
                            e.block.type = pt
                            e.block.blockData = pd
                            e.block.createDataContainer()["realms"] = Data(true)
                            e.block.blockData = e.block.blockData.also {
                                if (it is Waterlogged) {
                                    it.isWaterlogged = true
                                }
                            }
                            e.player.closeInventory()
                            e.player.playSound(e.player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
                            e.player.sendHolographic(e.block.location.toCenterLocation().add(0.0, 1.0, 0.0), "&e:)", "&f???????????????")
                        }

                        override fun generateItem(
                            player: Player,
                            element: RealmBlock,
                            index: Int,
                            slot: Int,
                        ): ItemStack {
                            return ItemBuilder(XMaterial.PAPER)
                                .name("&f?????? ${element.center.blockX},${element.center.blockY},${element.center.blockZ}")
                                .lore(
                                    "&7????????? &e${Coerce.format(player.location.distance(element.center))} &7???",
                                    "&7??????????????????"
                                ).colored().build()
                        }
                    }.open()
                }
            }
        }
    }

    fun Permission.register() {
        if (!permissions.contains(this)) {
            permissions.add(this)
        }
    }

    fun Location.getRealm(): RealmBlock? {
        return world.realms().firstOrNull { it.inside(this) }
    }

    fun getRealmBlock(location: Location): RealmBlock? {
        return location.getRealm()
    }

    fun Block.isRealmBlock(): Boolean {
        return getRealmBlock() != null
    }

    fun Block.getRealmBlock(): RealmBlock? {
        return world.realms().firstOrNull { it.center == location || it.extends.any { p -> p.key == Position.at(location) } }
    }

    fun ItemStack.getRealmSize(): Int {
        return if (Items.nonNull(this)) NMS.handle().loadNBT(this).getOrElse("realm-size", NBTBase(-1)).asInt() else -1
    }

    fun ItemStack.setRealmSize(value: Int) {
        val compound = NMS.handle().loadNBT(this)
        compound["realm-size"] = NBTBase(value)
        compound.saveTo(this)
    }

    fun World.realms(): List<RealmBlock> {
        return realmWorld().realms.values.flatten()
    }

    fun World.realmWorld(): RealmWorld {
        return worlds.computeIfAbsent(name) { RealmWorld() }
    }

    fun RealmBlock.create(player: Player) {
        users.computeIfAbsent(player.name) { HashMap() }["admin"] = true
        center.world.realmWorld().realms.computeIfAbsent(center.chunk.chunkKey) { ArrayList() }.add(this)
        save()
    }

    fun RealmBlock.save() {
        center.chunk.persistentDataContainer.set(NamespacedKey(Realms.plugin, node), PersistentDataType.STRING, json)
    }

    fun RealmBlock.remove() {
        center.world.realmWorld().realms[center.chunk.chunkKey]?.remove(this)
        center.chunk.persistentDataContainer.remove(NamespacedKey(Realms.plugin, node))
    }

    fun RealmBlock.getAdmin(): String {
        return users.keys.firstOrNull { hasPermission("admin", it, false) }!!
    }

    fun RealmBlock.isAdmin(player: Player): Boolean {
        return getAdmin() == player.name || player.isOp
    }

    fun RealmBlock.editName(player: Player) {
        Features.inputSign(player, arrayOf("", "", "????????????????????????")) { les ->
            val names = "${les[0]}${les[1]}".screen()
            if (names.isEmpty()) {
                player.error("???????????????!")
                return@inputSign
            }
            name = names
            player.info("?????????????????????????????? &f$names")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editJoinTell(player: Player) {
        Features.inputSign(player, arrayOf("", "", "????????????????????????")) { les ->
            val info = "${les[0]} | ${les[1]}".screen()
            if (info.isEmpty()) {
                player.error("???????????????!")
                return@inputSign
            }
            joinTell = info
            player.info("????????????")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.editLeaveTell(player: Player) {
        Features.inputSign(player, arrayOf("", "", "????????????????????????")) { les ->
            val info = "${les[0]} | ${les[1]}".screen()
            if (info.isEmpty()) {
                player.error("???????????????!")
                return@inputSign
            }
            leaveTell = info
            player.info("????????????")
            openSettings(player)
            save()
        }
    }

    fun RealmBlock.open(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        MenuBuilder.builder(Realms.plugin)
            .title("????????????")
            .rows(3)
            .items("", "#0#1#2#3#")
            .put(
                '0', ItemBuilder(XMaterial.PAINTING).name("&f????????????")
                    .lore(
                        "&7??????: &f${this.name}",
                        "&7?????????: &f${this.getAdmin()}",
                    )
                    .colored().build()
            )
            .put('1', ItemBuilder(XMaterial.COMMAND_BLOCK).name("&f????????????").lore("&7????????????????????????").colored().build())
            .put('2', ItemBuilder(XMaterial.CHAIN_COMMAND_BLOCK).name("&f????????????").lore("&7????????????????????????").colored().build())
            .put('3', ItemBuilder(XMaterial.OBSERVER).name("&f????????????").lore("&7?????????????????????").colored().build())
            .click { e ->
                when (e.slot) {
                    '1' -> openPermissionWorld(player)
                    '2' -> openPermissionUsers(player)
                    '3' -> openSettings(player)
                }
            }.open(player)
    }

    fun RealmBlock.openSettings(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        MenuBuilder.builder(Realms.plugin)
            .title("???????????? [????????????]")
            .rows(3)
            .items("", "#012")
            .put(
                '0', ItemBuilder(XMaterial.NAME_TAG).name("&f????????????")
                    .lore(
                        "&7????????????: &f${name}",
                        "",
                        "&7????????????:",
                        "&8?????????????????????????????????"
                    ).colored().build()
            )
            .put(
                '1', ItemBuilder(XMaterial.NAME_TAG).name("&f????????????")
                    .lore(
                        "&7????????????: &f${joinTell}",
                        "",
                        "&7????????????:",
                        "&8????????????????????????"
                    ).colored().build()
            )
            .put(
                '2', ItemBuilder(XMaterial.NAME_TAG).name("&f????????????")
                    .lore(
                        "&7????????????: &f${leaveTell}",
                        "",
                        "&7????????????:",
                        "&8????????????????????????"
                    ).colored().build()
            )
            .click { e ->
                when (e.slot) {
                    '0' -> {
                        editName(player)
                    }
                    '1' -> {
                        editJoinTell(player)
                    }
                    '2' -> {
                        editLeaveTell(player)
                    }
                }
            }.open(player)
    }

    fun RealmBlock.openPermissionWorld(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        object : MenuLinked<Permission>(player) {

            init {
                addButtonPreviousPage(47)
                addButtonNextPage(51)
            }

            override fun getTitle() = "???????????? [????????????]"

            override fun getRows() = 6

            override fun getElements(): List<Permission> {
                val list = RealmManager.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                if (!player.isOp) {
                    list.removeAll(list.filter { it.adminSide == player.isOp })
                }
                return list
            }

            override fun getSlots() = Items.INVENTORY_CENTER.toList()

            override fun onBuild(inventory: Inventory) {
                if (hasPreviousPage()) {
                    inventory.setItem(47, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(47, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
                if (hasNextPage()) {
                    inventory.setItem(51, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(51, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
            }

            override fun onClick(event: ClickEvent, element: Permission) {
                if (element.adminSide && !player.isOp) {
                    event.clicker.error("???????????????!")
                    return
                }
                permissions[element.id] = !hasPermission(element.id, def = element.default)
                open(page)
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                save()
            }

            override fun generateItem(player: Player, element: Permission, index: Int, slot: Int): ItemStack {
                if (element.adminSide && !player.isOp) {
                    return ItemStack(Material.BARRIER)
                }
                return element.generateMenuItem(hasPermission(element.id, def = element.default))
            }
        }.open()
    }

    fun RealmBlock.openPermissionUsers(player: Player) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        object : MenuLinked<String>(player) {

            init {
                addButtonPreviousPage(47)
                addButtonNextPage(51)
                addButton(49) {
                    Features.inputSign(player, arrayOf("", "", "??????????????????????????????")) {
                        val playerExact = Bukkit.getPlayerExact(it[0])
                        when {
                            playerExact == null -> {
                                player.error("??c??????${it[0]}????????????")
                            }
                            playerExact.name == player.name -> {
                                player.error("??c?????????????????????")
                            }
                            else -> {
                                users[playerExact.name] = HashMap()
                                save()
                                openPermissionUsers(player)
                            }
                        }
                    }
                }
            }

            override fun getTitle() = "???????????? [????????????]"

            override fun getRows() = 6

            override fun getElements() = users.keys.filter { it != player.name }.toList()

            override fun getSlots() = Items.INVENTORY_CENTER.toList()

            override fun onBuild(inventory: Inventory) {
                if (hasPreviousPage()) {
                    inventory.setItem(47, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(47, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
                if (hasNextPage()) {
                    inventory.setItem(51, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(51, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
                inventory.setItem(
                    49, ItemBuilder(XMaterial.WRITABLE_BOOK)
                        .name("&f????????????")
                        .lore(
                            "&7???????????????????????????????????????",
                        ).colored().build()
                )
            }

            override fun onClick(event: ClickEvent, user: String) {
                openPermissionUser(player, user)
            }

            override fun generateItem(player: Player, user: String, index: Int, slot: Int): ItemStack {
                return if (hasPermission("admin", user)) {
                    ItemBuilder(XMaterial.PLAYER_HEAD).name("&c????????? $user").lore("&7??????????????????").skullOwner(user).colored()
                        .build()
                } else {
                    ItemBuilder(XMaterial.PLAYER_HEAD).name("&f?????? $user").lore("&7??????????????????").skullOwner(user).colored()
                        .build()
                }
            }
        }.open()
    }

    fun RealmBlock.openPermissionUser(player: Player, user: String) {
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
        object : MenuLinked<Permission>(player) {

            init {
                addButtonPreviousPage(47)
                addButtonNextPage(51)
                addButton(49) {
                    users.remove(user)
                    save()
                    openPermissionUsers(player)
                }
            }

            override fun getTitle() = "???????????? [???????????? : $user]"

            override fun getRows() = 6

            override fun getElements(): List<Permission> {
                val list = RealmManager.permissions.filter { it.worldSide }.sortedBy { it.priority }.toMutableList()
                list.toList().forEach {
                    if (it.adminSide && !player.isOp) {
                        list.remove(it)
                    }
                }
                return list
            }

            override fun getSlots() = Items.INVENTORY_CENTER.toList()

            override fun onBuild(inventory: Inventory) {
                if (hasPreviousPage()) {
                    inventory.setItem(47, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(47, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
                if (hasNextPage()) {
                    inventory.setItem(51, ItemBuilder(XMaterial.SPECTRAL_ARROW).name("&f?????????").colored().build())
                } else {
                    inventory.setItem(51, ItemBuilder(XMaterial.ARROW).name("&8?????????").colored().build())
                }
                inventory.setItem(
                    49, ItemBuilder(XMaterial.LAVA_BUCKET)
                        .name("&4????????????")
                        .lore(
                            "&c????????????????????????????????????",
                        ).colored().build()
                )
            }

            override fun onClick(event: ClickEvent, element: Permission) {
                users[user]!![element.id] = !hasPermission(element.id, player = user, def = element.default)
                open(page)
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
                save()
            }

            override fun generateItem(player: Player, element: Permission, index: Int, slot: Int): ItemStack {
                return element.generateMenuItem(hasPermission(element.id, player = user, def = element.default))
            }
        }.open()
    }

    fun Chunk.realms() = persistentDataContainer.keys.filter { it.key.startsWith("realm_") }.map { realm ->
        val position = realm.key.substring("realm_".length).split("_")
        val json = JsonParser.parseString(persistentDataContainer[realm, PersistentDataType.STRING]).asJsonObject
        RealmBlock(position.toLocation(world), json["size"].asInt).also { realmBlock ->
            json["permissions"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.permissions[k] = v.asBoolean
            }
            json["users"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.users[k] = v.asJsonObject.entrySet().map { it.key to it.value.asBoolean }.toMap(HashMap())
            }
            json["extends"].asJsonObject.entrySet().forEach { (k, v) ->
                realmBlock.extends[Position.at(k.split(",").toLocation(world))] = v.asInt
            }
            json["name"].asString.also { name ->
                realmBlock.name = name
            }
            json["joinTell"].asString.also { value ->
                realmBlock.joinTell = value
            }
            json["leaveTell"].asString.also { value ->
                realmBlock.leaveTell = value
            }
            realmBlock.update()
        }
    }

    private fun List<String>.toLocation(world: World): Location {
        return Location(world, Coerce.toDouble(this[0]), Coerce.toDouble(this[1]), Coerce.toDouble(this[2]))
    }

    @EventHandler
    fun onJoinEvent(event: RealmsJoinEvent) {
        val realm = event.realmBlock ?: return
        val message = realm.joinTell.split(" | ")
        if (message.size >= 2) {
            TLocale.Display.sendTitle(event.player, message[0], message[1], 20, 40, 20)
            return
        }
        TLocale.Display.sendTitle(event.player, message[0], "", 20, 40, 20)
    }

    @EventHandler
    fun onLeaveEvent(event: RealmsLeaveEvent) {
        val realm = event.realmBlock ?: return
        val message = realm.leaveTell.split(" | ")
        if (message.size >= 2) {
            TLocale.Display.sendTitle(event.player, message[0], message[1], 20, 40, 20)
            return
        }
        TLocale.Display.sendTitle(event.player, message[0], "", 20, 40, 20)
    }
}