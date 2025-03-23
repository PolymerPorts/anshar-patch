package eu.pb4.ansharpatch.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
//    @Shadow @Final List<ServerPlayerEntity> players;
//
//    @Redirect(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1))
//    private void cancelRainPacket(PlayerManager instance, Packet<?> packet) {
//        for (ServerPlayerEntity serverPlayerEntity : this.players) {
//            if (PlayerTransportComponent.KEY.get(serverPlayerEntity).isInNetwork()) {
//                return;
//            }
//            serverPlayerEntity.networkHandler.sendPacket(packet);
//        }
//    }
}
