package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class KeyTagProvider extends TagsProvider {
   protected KeyTagProvider(final PackOutput output, final ResourceKey registryKey, final CompletableFuture lookupProvider) {
      super(output, registryKey, lookupProvider);
   }

   protected TagAppender tag(final TagKey tag) {
      TagBuilder builder = this.getOrCreateRawBuilder(tag);
      return TagAppender.forBuilder(builder);
   }

   protected TagAppender tag(final TagKey tag, final boolean replace) {
      TagBuilder builder = this.getOrCreateRawBuilder(tag);
      builder.setReplace(replace);
      return TagAppender.forBuilder(builder);
   }
}
