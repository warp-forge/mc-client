package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate(TagKey tag, boolean expected) {
   public static Codec codec(final ResourceKey registryKey) {
      return RecordCodecBuilder.create((i) -> i.group(TagKey.codec(registryKey).fieldOf("id").forGetter(TagPredicate::tag), Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)).apply(i, TagPredicate::new));
   }

   public static TagPredicate is(final TagKey tag) {
      return new TagPredicate(tag, true);
   }

   public static TagPredicate isNot(final TagKey tag) {
      return new TagPredicate(tag, false);
   }

   public boolean matches(final Holder holder) {
      return holder.is(this.tag) == this.expected;
   }
}
