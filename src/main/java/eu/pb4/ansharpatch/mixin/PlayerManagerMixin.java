package eu.pb4.ansharpatch.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
//    @WrapOperation(method = "sendWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 3))
//    private void cancelRainPacket(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
//        if (PlayerTransportComponent.KEY.get(instance.player).isInNetwork()) {
//            return;
//        }
//        original.call(instance, packet);
//    }
}
