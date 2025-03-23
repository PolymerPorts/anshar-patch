package eu.pb4.ansharpatch.mixin;

import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FileResourcePackProvider.class)
public interface FileResourcePackProviderAccessor {
    @Accessor
    ResourcePackSource getSource();
}
