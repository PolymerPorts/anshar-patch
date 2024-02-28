package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "updateInput", at = @At("TAIL"))
    private void orIsInNetwork(float sidewaysSpeed, float forwardSpeed, boolean jumping, boolean sneaking, CallbackInfo ci) {
        if (PlayerTransportComponent.KEY.get(this).isInNetwork()) {
            if (sidewaysSpeed >= -1.0F && sidewaysSpeed <= 1.0F) {
                this.sidewaysSpeed = sidewaysSpeed;
            }

            if (forwardSpeed >= -1.0F && forwardSpeed <= 1.0F) {
                this.forwardSpeed = forwardSpeed;
            }

            this.jumping = jumping;
            this.setSneaking(sneaking);
        };
    }
}
