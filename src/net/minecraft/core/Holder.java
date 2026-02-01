package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public interface Holder {
   Object value();

   boolean isBound();

   boolean areComponentsBound();

   boolean is(Identifier key);

   boolean is(ResourceKey key);

   boolean is(Predicate predicate);

   boolean is(TagKey tag);

   /** @deprecated */
   @Deprecated
   boolean is(Holder holder);

   Stream tags();

   DataComponentMap components();

   Either unwrap();

   Optional unwrapKey();

   Kind kind();

   boolean canSerializeIn(HolderOwner registry);

   default String getRegisteredName() {
      return (String)this.unwrapKey().map((key) -> key.identifier().toString()).orElse("[unregistered]");
   }

   static Holder direct(final Object value) {
      return new Direct(value, DataComponentMap.EMPTY);
   }

   static Holder direct(final Object value, final DataComponentMap components) {
      return new Direct(value, components);
   }

   public static enum Kind {
      REFERENCE,
      DIRECT;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{REFERENCE, DIRECT};
      }
   }

   public static record Direct(Object value, DataComponentMap components) implements Holder {
      public boolean isBound() {
         return true;
      }

      public boolean areComponentsBound() {
         return true;
      }

      public boolean is(final Identifier key) {
         return false;
      }

      public boolean is(final ResourceKey key) {
         return false;
      }

      public boolean is(final TagKey tag) {
         return false;
      }

      public boolean is(final Holder holder) {
         return this.value.equals(holder.value());
      }

      public boolean is(final Predicate predicate) {
         return false;
      }

      public Either unwrap() {
         return Either.right(this.value);
      }

      public Optional unwrapKey() {
         return Optional.empty();
      }

      public Kind kind() {
         return Holder.Kind.DIRECT;
      }

      public String toString() {
         return "Direct{" + String.valueOf(this.value) + "}";
      }

      public boolean canSerializeIn(final HolderOwner registry) {
         return true;
      }

      public Stream tags() {
         return Stream.of();
      }
   }

   public static class Reference implements Holder {
      private final HolderOwner owner;
      private @Nullable Set tags;
      private @Nullable DataComponentMap components;
      private final Type type;
      private @Nullable ResourceKey key;
      private @Nullable Object value;

      protected Reference(final Type type, final HolderOwner owner, final @Nullable ResourceKey key, final @Nullable Object value) {
         this.owner = owner;
         this.type = type;
         this.key = key;
         this.value = value;
      }

      public static Reference createStandAlone(final HolderOwner owner, final ResourceKey key) {
         return new Reference(Holder.Reference.Type.STAND_ALONE, owner, key, (Object)null);
      }

      /** @deprecated */
      @Deprecated
      public static Reference createIntrusive(final HolderOwner owner, final @Nullable Object value) {
         return new Reference(Holder.Reference.Type.INTRUSIVE, owner, (ResourceKey)null, value);
      }

      public ResourceKey key() {
         if (this.key == null) {
            String var10002 = String.valueOf(this.value);
            throw new IllegalStateException("Trying to access unbound value '" + var10002 + "' from registry " + String.valueOf(this.owner));
         } else {
            return this.key;
         }
      }

      public Object value() {
         if (this.value == null) {
            String var10002 = String.valueOf(this.key);
            throw new IllegalStateException("Trying to access unbound value '" + var10002 + "' from registry " + String.valueOf(this.owner));
         } else {
            return this.value;
         }
      }

      public boolean is(final Identifier key) {
         return this.key().identifier().equals(key);
      }

      public boolean is(final ResourceKey key) {
         return this.key() == key;
      }

      private Set boundTags() {
         if (this.tags == null) {
            throw new IllegalStateException("Tags not bound");
         } else {
            return this.tags;
         }
      }

      public boolean is(final TagKey tag) {
         return this.boundTags().contains(tag);
      }

      public boolean is(final Holder holder) {
         return holder.is(this.key());
      }

      public boolean is(final Predicate predicate) {
         return predicate.test(this.key());
      }

      public boolean canSerializeIn(final HolderOwner context) {
         return this.owner.canSerializeIn(context);
      }

      public Either unwrap() {
         return Either.left(this.key());
      }

      public Optional unwrapKey() {
         return Optional.of(this.key());
      }

      public Kind kind() {
         return Holder.Kind.REFERENCE;
      }

      public boolean isBound() {
         return this.key != null && this.value != null;
      }

      public boolean areComponentsBound() {
         return this.components != null;
      }

      void bindKey(final ResourceKey key) {
         if (this.key != null && key != this.key) {
            String var10002 = String.valueOf(this.key);
            throw new IllegalStateException("Can't change holder key: existing=" + var10002 + ", new=" + String.valueOf(key));
         } else {
            this.key = key;
         }
      }

      protected void bindValue(final Object value) {
         if (this.type == Holder.Reference.Type.INTRUSIVE && this.value != value) {
            String var10002 = String.valueOf(this.key);
            throw new IllegalStateException("Can't change holder " + var10002 + " value: existing=" + String.valueOf(this.value) + ", new=" + String.valueOf(value));
         } else {
            this.value = value;
         }
      }

      void bindTags(final Collection tags) {
         this.tags = Set.copyOf(tags);
      }

      public void bindComponents(final DataComponentMap components) {
         this.components = components;
      }

      public Stream tags() {
         return this.boundTags().stream();
      }

      public DataComponentMap components() {
         return (DataComponentMap)Objects.requireNonNull(this.components, "Components not bound yet");
      }

      public String toString() {
         String var10000 = String.valueOf(this.key);
         return "Reference{" + var10000 + "=" + String.valueOf(this.value) + "}";
      }

      protected static enum Type {
         STAND_ALONE,
         INTRUSIVE;

         // $FF: synthetic method
         private static Type[] $values() {
            return new Type[]{STAND_ALONE, INTRUSIVE};
         }
      }
   }
}
