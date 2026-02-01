package net.minecraft.data.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public interface TagAppender {
   TagAppender add(Object element);

   default TagAppender add(final Object... elements) {
      return this.addAll(Arrays.stream(elements));
   }

   default TagAppender addAll(final Collection elements) {
      elements.forEach(this::add);
      return this;
   }

   default TagAppender addAll(final Stream elements) {
      elements.forEach(this::add);
      return this;
   }

   TagAppender addOptional(Object element);

   TagAppender addTag(TagKey tag);

   TagAppender addOptionalTag(TagKey tag);

   static TagAppender forBuilder(final TagBuilder builder) {
      return new TagAppender() {
         public TagAppender add(final ResourceKey element) {
            builder.addElement(element.identifier());
            return this;
         }

         public TagAppender addOptional(final ResourceKey element) {
            builder.addOptionalElement(element.identifier());
            return this;
         }

         public TagAppender addTag(final TagKey tag) {
            builder.addTag(tag.location());
            return this;
         }

         public TagAppender addOptionalTag(final TagKey tag) {
            builder.addOptionalTag(tag.location());
            return this;
         }
      };
   }

   default TagAppender map(final Function converter) {
      return new TagAppender() {
         {
            Objects.requireNonNull(TagAppender.this);
         }

         public TagAppender add(final Object element) {
            TagAppender.this.add(converter.apply(element));
            return this;
         }

         public TagAppender addOptional(final Object element) {
            TagAppender.this.add(converter.apply(element));
            return this;
         }

         public TagAppender addTag(final TagKey tag) {
            TagAppender.this.addTag(tag);
            return this;
         }

         public TagAppender addOptionalTag(final TagKey tag) {
            TagAppender.this.addOptionalTag(tag);
            return this;
         }
      };
   }
}
