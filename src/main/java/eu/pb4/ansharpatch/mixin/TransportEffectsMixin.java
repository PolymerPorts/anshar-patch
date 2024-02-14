package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.transport.TransportEffects;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TransportEffects.class)
public class TransportEffectsMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/particle/v1/FabricParticleTypes;simple(Z)Lnet/minecraft/particle/DefaultParticleType;"))
    private static DefaultParticleType replaceParticle(boolean alwaysSpawn) {
        return ParticleTypes.FIREWORK;
    }
}
