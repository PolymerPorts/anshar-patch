package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @WrapOperation(method = "updateInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;hasVehicle()Z"))
    private boolean orIsInNetwork(ServerPlayerEntity instance, Operation<Boolean> original) {
        return original.call(instance) || PlayerTransportComponent.KEY.get(instance).isInNetwork();
    }
}
