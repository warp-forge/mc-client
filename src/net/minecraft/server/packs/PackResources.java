package net.minecraft.server.packs;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public interface PackResources extends AutoCloseable {
   String METADATA_EXTENSION = ".mcmeta";
   String PACK_META = "pack.mcmeta";

   @Nullable IoSupplier getRootResource(String... path);

   @Nullable IoSupplier getResource(PackType type, Identifier location);

   void listResources(PackType type, String namespace, String directory, ResourceOutput output);

   Set getNamespaces(PackType type);

   @Nullable Object getMetadataSection(MetadataSectionType metadataSerializer) throws IOException;

   PackLocationInfo location();

   default String packId() {
      return this.location().id();
   }

   default Optional knownPackInfo() {
      return this.location().knownPackInfo();
   }

   void close();

   @FunctionalInterface
   public interface ResourceOutput extends BiConsumer {
   }
}
