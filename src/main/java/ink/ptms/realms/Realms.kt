package ink.ptms.realms

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.util.item.Items
import org.bukkit.inventory.ItemStack

object Realms : Plugin() {

    @TInject
    lateinit var conf: TConfig
        private set

    val realmsDust: ItemStack
        get() = Items.loadItem(conf.getConfigurationSection("realms-dust"))!!
}
