package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider extends TagsProvider {
   private final Function keyExtractor;

   public IntrinsicHolderTagsProvider(final PackOutput output, final ResourceKey registryKey, final CompletableFuture lookupProvider, final Function keyExtractor) {
      super(output, registryKey, lookupProvider);
      this.keyExtractor = keyExtractor;
   }

   public IntrinsicHolderTagsProvider(final PackOutput output, final ResourceKey registryKey, final CompletableFuture lookupProvider, final CompletableFuture parentProvider, final Function keyExtractor) {
      super(output, registryKey, lookupProvider, parentProvider);
      this.keyExtractor = keyExtractor;
   }

   protected TagAppender tag(final TagKey tag) {
      TagBuilder builder = this.getOrCreateRawBuilder(tag);
      return TagAppender.forBuilder(builder).map(this.keyExtractor);
   }
}
