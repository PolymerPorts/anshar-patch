package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @ModifyExpressionValue(method = "updateInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"))
    private boolean orIsInNetwork(boolean value) {
        return value || PlayerTransportComponent.KEY.get(this).isInNetwork();
    }
}
