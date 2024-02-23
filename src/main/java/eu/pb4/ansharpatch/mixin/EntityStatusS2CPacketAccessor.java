package eu.pb4.ansharpatch.mixin;

import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityStatusS2CPacket.class)
public interface EntityStatusS2CPacketAccessor {
    @Accessor
    int getId();

    @Mutable
    @Accessor
    void setId(int id);

    @Accessor
    byte getStatus();

    @Mutable
    @Accessor
    void setStatus(byte status);
}
