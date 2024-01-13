package org.valkyrienskies.tournament.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import net.minecraft.core.BlockPos
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.util.extension.void
import java.util.concurrent.CopyOnWriteArrayList

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
/**
 * for compat only!!
 * @see TournamentShips
 */
@Deprecated("Use TournamentShips instead")
class BalloonShipControl : ShipForcesInducer {

    val balloons = CopyOnWriteArrayList<Pair<Vector3i, Double>>()

    override fun applyForces(physShip: PhysShip) =
        void()

}