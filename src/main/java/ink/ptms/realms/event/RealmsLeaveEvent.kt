package ink.ptms.realms.event

import ink.ptms.realms.data.RealmBlock
import io.izzel.taboolib.module.event.EventCancellable
import org.bukkit.entity.Player

class RealmsLeaveEvent(
    val player: Player,
    val realmBlock: RealmBlock?,
    val oldRealmBlock: RealmBlock?,
) : EventCancellable<RealmsLeaveEvent>()