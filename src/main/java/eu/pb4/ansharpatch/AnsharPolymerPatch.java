package eu.pb4.ansharpatch;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnsharPolymerPatch implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("anshar-polymer-patch");

	@Override
	public void onInitialize() {
		/*PolymerResourcePackUtils.addModAssets("anshar");
		PolymerResourcePackUtils.getInstance().creationEvent.register(x -> {
			x.addWriteConverter((path, bytes) -> path.startsWith("/assets/anshar/sounds/tunes") ? null : bytes);
		});
		 */
	}
}