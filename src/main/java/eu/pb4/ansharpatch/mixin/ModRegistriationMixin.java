package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.ModRegistration;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModRegistration.class)
public class ModRegistriationMixin {
    @WrapOperation(method = "registerAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;register(Lnet/minecraft/registry/Registry;Lnet/minecraft/util/Identifier;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <V, T extends V> T cancelRegistration(Registry<V> registry, Identifier id, T entry, Operation<T> original) {
        if (Registries.DATA_COMPONENT_TYPE.equals(registry)) {
            return original.call(registry, id, entry);
        }
        return entry;
    }

    @Redirect(method = "registerAll", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking;registerGlobalReceiver(Lnet/minecraft/network/packet/CustomPayload$Id;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$PlayPayloadHandler;)Z"))
    private static boolean cancelNetworking(CustomPayload.Id<?> type, ServerPlayNetworking.PlayPayloadHandler<?> handler) {
        return false;
    }
}
