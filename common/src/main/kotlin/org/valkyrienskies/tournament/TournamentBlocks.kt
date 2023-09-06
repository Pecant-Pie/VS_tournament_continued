package org.valkyrienskies.tournament

import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FireBlock
import net.minecraft.world.level.block.OreBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import org.valkyrienskies.mod.common.hooks.VSGameEvents
import org.valkyrienskies.tournament.util.extension.explodeShip
import org.valkyrienskies.tournament.blocks.*
import org.valkyrienskies.tournament.blocks.explosive.AbstractExplosiveBlock
import org.valkyrienskies.tournament.blocks.explosive.SimpleExplosiveStagedBlock
import org.valkyrienskies.tournament.blocks.explosive.TestExplosiveBlock
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.registry.RegistrySupplier

@Suppress("unused")
object TournamentBlocks {
    private val BLOCKS = DeferredRegister.create(TournamentMod.MOD_ID, Registry.BLOCK_REGISTRY)
    private val ITEMS = ArrayList<Pair<String, ()->Item>>()


    lateinit var SHIP_ASSEMBLER           : RegistrySupplier<ShipAssemblerBlock>
    lateinit var BALLAST                  : RegistrySupplier<BallastBlock>
    lateinit var POWERED_BALLOON          : RegistrySupplier<BalloonBlock>
    lateinit var BALLOON                  : RegistrySupplier<BalloonBlock>
    lateinit var THRUSTER                 : RegistrySupplier<ThrusterBlock>
    lateinit var THRUSTER_TINY            : RegistrySupplier<ThrusterBlock>
    lateinit var SPINNER                  : RegistrySupplier<SpinnerBlock>
    lateinit var SEAT                     : RegistrySupplier<SeatBlock>
    lateinit var ROPE_HOOK                : RegistrySupplier<RopeHookBlock>
    lateinit var SENSOR                   : RegistrySupplier<SensorBlock>

    lateinit var EXPLOSIVE_INSTANT_SMALL  : RegistrySupplier<AbstractExplosiveBlock>
    lateinit var EXPLOSIVE_INSTANT_MEDIUM : RegistrySupplier<AbstractExplosiveBlock>
    lateinit var EXPLOSIVE_INSTANT_LARGE  : RegistrySupplier<AbstractExplosiveBlock>

    lateinit var EXPLOSIVE_STAGED_SMALL   : RegistrySupplier<AbstractExplosiveBlock>

    lateinit var EXPLOSIVE_TEST           : RegistrySupplier<TestExplosiveBlock>

    lateinit var FUEL_CONTAINER_FULL      : RegistrySupplier<FuelContainerBlock>

    lateinit var FUEL_GAUGE               : RegistrySupplier<FuelGaugeBlock>


    fun register() {
        SHIP_ASSEMBLER           = register("ship_assembler", ::ShipAssemblerBlock)
        BALLAST                  = register("ballast", ::BallastBlock)
        POWERED_BALLOON          = register("balloon", ::PoweredBalloonBlock)
        BALLOON                  = register("balloon_unpowered", ::BalloonBlock)
        THRUSTER                 = register("thruster") { ThrusterBlock(
            1.0,
            ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
            5
        )}
        THRUSTER_TINY            = register("tiny_thruster") { ThrusterBlock(
            0.2,
            ParticleTypes.CAMPFIRE_COSY_SMOKE,
            3
        ) }
        SPINNER                  = register("spinner", ::SpinnerBlock)
        SEAT                     = register("seat", ::SeatBlock)
        SENSOR                   = register("sensor", ::SensorBlock)

        EXPLOSIVE_INSTANT_SMALL  = register("explosive_instant_small") { object : AbstractExplosiveBlock() {
            override fun explode(level: ServerLevel, pos: BlockPos) {
                level.explodeShip(level, pos.x+0.5, pos.y+0.5, pos.z+0.5, 3.0f, Explosion.BlockInteraction.BREAK)
            }}
        }
        EXPLOSIVE_INSTANT_MEDIUM = register("explosive_instant_medium") { object : AbstractExplosiveBlock() {
            override fun explode(level: ServerLevel, pos: BlockPos) {
                level.explodeShip(level, pos.x+0.5, pos.y+0.5, pos.z+0.5, 6.0f, Explosion.BlockInteraction.BREAK)
            }}
        }
        EXPLOSIVE_INSTANT_LARGE  = register("explosive_instant_large") { object : AbstractExplosiveBlock() {
            override fun explode(level: ServerLevel, pos: BlockPos) {
                level.explodeShip(level, pos.x+0.5, pos.y+0.5, pos.z+0.5, 12.0f, Explosion.BlockInteraction.BREAK)
            }}
        }
        EXPLOSIVE_STAGED_SMALL   = register("explosive_staged_small") {
            object : SimpleExplosiveStagedBlock(
                (3..7),
                (4..7),
                (-10..10),
                (-2..2),
                (-10..10),
                Explosion.BlockInteraction.BREAK
            ) {}
        }
        EXPLOSIVE_TEST           = register("explosive_test", ::TestExplosiveBlock)

        FUEL_CONTAINER_FULL      = register("fuel_container_full", ::FuelContainerBlock)

        FUEL_GAUGE               = register("fuel_gauge", ::FuelGaugeBlock)

        register("ore_phynite") {
            OreBlock(BlockBehaviour.Properties.of(TournamentMaterials.PHYNITE)
                .strength(3.0f, 3.0f)
            )
        }


        // old:
        register("shipifier", null) { OldBlock(SHIP_ASSEMBLER.get()) }
        register("instantexplosive", null) { OldBlock(EXPLOSIVE_INSTANT_MEDIUM.get()) }
        register("instantexplosive_big", null) { OldBlock(EXPLOSIVE_INSTANT_LARGE.get()) }
        register("stagedexplosive", null) { OldBlock(EXPLOSIVE_STAGED_SMALL.get()) }
        register("stagedexplosive_big", null) { OldBlock(Blocks.AIR) }
        ROPE_HOOK                = register("rope_hook", ::RopeHookBlock)


        BLOCKS.applyAll()
        VSGameEvents.registriesCompleted.on { _, _ ->
            makeFlammables()
        }
    }

    private fun <T: Block> register(name: String, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(TournamentItems.TAB)) })
        return supplier
    }

    private fun <T: Block> register(name: String, tab: CreativeModeTab?, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        tab?.let {
            ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(tab)) })
        }
        return supplier
    }

    private fun <T: Block> register(name: String, block: () -> T, item: () -> Item): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to item)
        return supplier
    }

    fun flammableBlock(block: Block, flameOdds: Int, burnOdds: Int) {
        val fire = Blocks.FIRE as FireBlock
        fire.setFlammable(block, flameOdds, burnOdds)
    }

    fun makeFlammables() {
        flammableBlock(SEAT.get(), 15, 25)
        flammableBlock(POWERED_BALLOON.get(), 30, 60)
    }

    fun registerItems(items: DeferredRegister<Item>) {
        ITEMS.forEach { items.register(it.first, it.second) }
    }

}
