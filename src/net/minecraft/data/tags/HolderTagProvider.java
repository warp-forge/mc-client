package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class HolderTagProvider extends TagsProvider {
   protected HolderTagProvider(final PackOutput output, final ResourceKey registryKey, final CompletableFuture lookupProvider) {
      super(output, registryKey, lookupProvider);
   }

   protected TagAppender tag(final TagKey tag) {
      TagBuilder builder = this.getOrCreateRawBuilder(tag);
      return TagAppender.forBuilder(builder).map(Holder.Reference::key);
   }
}
