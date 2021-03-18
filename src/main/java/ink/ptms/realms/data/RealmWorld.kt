package ink.ptms.realms.data

import java.util.concurrent.ConcurrentHashMap

/**
 * Realms
 * ink.ptms.realms.data.RealmWorld
 *
 * @author sky
 * @since 2021/3/11 10:47 下午
 */
class RealmWorld {

    val realms = ConcurrentHashMap<Long, MutableList<RealmBlock>>()
}