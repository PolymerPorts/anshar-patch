package eu.pb4.ansharpatch.pack;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public record LangPackProvider(ModContainer mod) implements ResourcePackProvider, ResourcePackProfile.PackFactory, ResourcePack {
    public static final ResourcePackProvider INSTANCE = new LangPackProvider(FabricLoader.getInstance().getModContainer("anshar").get());

    @Override
    public void register(Consumer<ResourcePackProfile> profileAdder) {
        profileAdder.accept(
                ResourcePackProfile.create(
                        getInfo(), 
                        this, 
                        ResourceType.SERVER_DATA, 
                        new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.BOTTOM, false)
                )
        );
    }

    @Override
    public ResourcePack open(ResourcePackInfo info) {
        return this;
    }

    @Override
    public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
        return open(info);
    }

    @Override
    public InputSupplier<InputStream> openRoot(String... segments) {
        var path = mod.findPath(String.join("/", segments));

        return path.map(InputSupplier::create).orElse(null);
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        var path = mod.findPath("assets/" + id.getNamespace() + "/" + id.getPath());

        return path.map(InputSupplier::create).orElse(null);
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        var optional = mod.findPath("assets/" + namespace + "/" + prefix);

        if (optional.isEmpty()) {
            return;
        }

        try {
            String separator = optional.get().getFileSystem().getSeparator();
            Files.walkFileTree(optional.get(), new SimpleFileVisitor<>() {
                @Override
                @NotNull
                public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                    String filename = optional.get().relativize(file).toString().replace(separator, "/");
                    Identifier identifier = Identifier.tryParse(namespace, prefix + "/" + filename);

                    if (identifier != null) {
                        consumer.accept(identifier, InputSupplier.create(file));
                    }

                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException ignored) {
            // NO-OP
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return Set.of("anshar");
    }

    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        return ResourceMetadataMap.of(PackResourceMetadata.SERIALIZER, new PackResourceMetadata(Text.literal("Anshar Polymer Patch"), SharedConstants.getGameVersion().getResourceVersion(ResourceType.SERVER_DATA), Optional.empty())).get(metaReader);
    }

    @Override
    public ResourcePackInfo getInfo() {
        return new ResourcePackInfo(
                "anshar-polymer-patch",
                Text.literal("Anshar Polymer Patch"),
                ResourcePackSource.BUILTIN,
                Optional.empty()
        );
    }

    @Override
    public void close() {

    }
}
