package ink.ptms.realms

import ink.ptms.realms.RealmManager.getRealmSize
import ink.ptms.realms.RealmManager.setRealmSize
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.CommandType
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.Items
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Realms
 * ink.ptms.realms.RealmCommand
 *
 * @author sky
 * @since 2021/3/11 10:54 下午
 */
@BaseCommand(name = "realm", permission = "admin")
class RealmCommand : BaseMainCommand() {

    @SubCommand(description = "设置领域大小", arguments = ["大小"], type = CommandType.PLAYER)
    fun setRealmSize(sender: Player, args: Array<String>) {
        if (Items.isNull(sender.itemInHand)) {
            sender.sendMessage("你无法给空气设置领域大小。")
            return
        }
        sender.itemInHand.setRealmSize(Coerce.toInteger(args[0]))
        sender.sendMessage("当前手中物品的领域大小为${sender.itemInHand.getRealmSize()}格。")
    }
}