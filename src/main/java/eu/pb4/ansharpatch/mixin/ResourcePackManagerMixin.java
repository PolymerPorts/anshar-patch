package eu.pb4.ansharpatch.mixin;

import eu.pb4.ansharpatch.pack.LangPackProvider;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {
	@Shadow @Final @Mutable private Set<ResourcePackProvider> providers;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void injectCustom(ResourcePackProvider[] resourcePackProviders, CallbackInfo info) {
		providers = new LinkedHashSet<>(providers);

		// Search resource pack providers to find any server-related pack provider.
		boolean shouldAddServerProvider = false;

		for (ResourcePackProvider provider : this.providers) {
			if (provider instanceof FileResourcePackProvider
					&& (((FileResourcePackProviderAccessor) provider).getSource() == ResourcePackSource.WORLD
					|| ((FileResourcePackProviderAccessor) provider).getSource() == ResourcePackSource.SERVER)) {
				shouldAddServerProvider = true;
				break;
			}
		}

		// On server, add the mod resource pack provider.
		if (shouldAddServerProvider) {
			providers.add(LangPackProvider.INSTANCE);
		}
	}
}