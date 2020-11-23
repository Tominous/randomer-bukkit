package hazae41.randomer

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Biome.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import kotlin.random.Random

class Main : JavaPlugin(), Listener {
  val maxDistance get() = config.getInt("max-chunk-distance")
  val maxInhabited get() = config.getInt("max-inhabited-seconds")

  override fun onEnable() {
    super.onEnable()

    saveDefaultConfig()

    server.pluginManager.registerEvents(this, this)
  }

  fun Biome.isBad() = when (this) {
    OCEAN,
    COLD_OCEAN,
    FROZEN_OCEAN,
    WARM_OCEAN,
    LUKEWARM_OCEAN -> true
    DEEP_COLD_OCEAN,
    DEEP_FROZEN_OCEAN,
    DEEP_WARM_OCEAN,
    DEEP_LUKEWARM_OCEAN -> true
    else -> false
  }

  fun World.randomLocation(): Location {
    var tries = 0

    while (true) {
      tries++

      val cx = Random.nextInt(-maxDistance, +maxDistance)
      val cz = Random.nextInt(-maxDistance, +maxDistance)
      val chunk = getChunkAt(cx, cz)

      if (tries == 4096) {
        logger.warning("Could not find a good place")
        val block = chunk.getBlock(7, 80, 7)
        return block.location
      }
      
      if (chunk.inhabitedTime > maxInhabited * 20)
        continue

      val block = chunk.getBlock(7, 80, 7)
      if (block.biome.isBad()) continue

      logger.info("Found a good place in $tries tries")

      return block.location
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  fun onspawn(e: PlayerSpawnLocationEvent) {
    if (e.player.hasPlayedBefore()) return
    val world = e.spawnLocation.world!!
    e.spawnLocation = world.randomLocation()
  }

  @EventHandler(priority = EventPriority.NORMAL)
  fun onrespawn(e: PlayerRespawnEvent) {
    if (e.isAnchorSpawn || e.isBedSpawn) return
    val world = e.respawnLocation.world!!
    e.respawnLocation = world.randomLocation()
  }
}