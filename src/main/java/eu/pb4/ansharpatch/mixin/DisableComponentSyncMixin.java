package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.beacon.BeaconComponent;
import com.lgmrszd.anshar.beacon.EndCrystalComponent;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BeaconComponent.class, EndCrystalComponent.class, PlayerTransportComponent.class})
public abstract class DisableComponentSyncMixin implements AutoSyncedComponent {

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return false;
    }
}
