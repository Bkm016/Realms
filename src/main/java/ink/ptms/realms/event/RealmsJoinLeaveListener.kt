package ink.ptms.realms.event

import ink.ptms.realms.RealmManager.getRealm
import io.izzel.taboolib.module.inject.TListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

@TListener
class RealmsJoinLeaveListener : Listener {
    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (event.from.x != event.to.x || event.from.y != event.from.y || event.from.z != event.to.z) {
            val form = event.from.getRealm()
            val to = event.to.getRealm()
            if (form != to) {
                RealmsJoinEvent(event.player, to, form).call().ifCancelled {
                    event.to = event.from
                }
                RealmsLeaveEvent(event.player, form, to).call().ifCancelled {
                    event.from = event.to
                }
            }
        }
    }

}