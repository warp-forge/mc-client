package net.minecraft.core;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface HolderSet extends Iterable {
   Stream stream();

   int size();

   boolean isBound();

   Either unwrap();

   Optional getRandomElement(RandomSource random);

   Holder get(int index);

   boolean contains(final Holder value);

   boolean canSerializeIn(HolderOwner owner);

   Optional unwrapKey();

   /** @deprecated */
   @Deprecated
   @VisibleForTesting
   static Named emptyNamed(final HolderOwner owner, final TagKey key) {
      return new Named(owner, key) {
         protected List contents() {
            throw new UnsupportedOperationException("Tag " + String.valueOf(this.key()) + " can't be dereferenced during construction");
         }
      };
   }

   static HolderSet empty() {
      return HolderSet.Direct.EMPTY;
   }

   @SafeVarargs
   static Direct direct(final Holder... values) {
      return new Direct(List.of(values));
   }

   static Direct direct(final List values) {
      return new Direct(List.copyOf(values));
   }

   @SafeVarargs
   static Direct direct(final Function holderGetter, final Object... elements) {
      return direct(Stream.of(elements).map(holderGetter).toList());
   }

   static Direct direct(final Function holderGetter, final Collection elements) {
      return direct(elements.stream().map(holderGetter).toList());
   }

   public abstract static class ListBacked implements HolderSet {
      protected abstract List contents();

      public int size() {
         return this.contents().size();
      }

      public Spliterator spliterator() {
         return this.contents().spliterator();
      }

      public Iterator iterator() {
         return this.contents().iterator();
      }

      public Stream stream() {
         return this.contents().stream();
      }

      public Optional getRandomElement(final RandomSource random) {
         return Util.getRandomSafe(this.contents(), random);
      }

      public Holder get(final int index) {
         return (Holder)this.contents().get(index);
      }

      public boolean canSerializeIn(final HolderOwner owner) {
         return true;
      }
   }

   public static final class Direct extends ListBacked {
      private static final Direct EMPTY = new Direct(List.of());
      private final List contents;
      private @Nullable Set contentsSet;

      private Direct(final List contents) {
         this.contents = contents;
      }

      protected List contents() {
         return this.contents;
      }

      public boolean isBound() {
         return true;
      }

      public Either unwrap() {
         return Either.right(this.contents);
      }

      public Optional unwrapKey() {
         return Optional.empty();
      }

      public boolean contains(final Holder value) {
         if (this.contentsSet == null) {
            this.contentsSet = Set.copyOf(this.contents);
         }

         return this.contentsSet.contains(value);
      }

      public String toString() {
         return "DirectSet[" + String.valueOf(this.contents) + "]";
      }

      public boolean equals(final Object obj) {
         if (this == obj) {
            return true;
         } else {
            boolean var10000;
            if (obj instanceof Direct) {
               Direct<?> direct = (Direct)obj;
               if (this.contents.equals(direct.contents)) {
                  var10000 = true;
                  return var10000;
               }
            }

            var10000 = false;
            return var10000;
         }
      }

      public int hashCode() {
         return this.contents.hashCode();
      }
   }

   public static class Named extends ListBacked {
      private final HolderOwner owner;
      private final TagKey key;
      private @Nullable List contents;

      Named(final HolderOwner owner, final TagKey key) {
         this.owner = owner;
         this.key = key;
      }

      void bind(final List contents) {
         this.contents = List.copyOf(contents);
      }

      public TagKey key() {
         return this.key;
      }

      protected List contents() {
         if (this.contents == null) {
            String var10002 = String.valueOf(this.key);
            throw new IllegalStateException("Trying to access unbound tag '" + var10002 + "' from registry " + String.valueOf(this.owner));
         } else {
            return this.contents;
         }
      }

      public boolean isBound() {
         return this.contents != null;
      }

      public Either unwrap() {
         return Either.left(this.key);
      }

      public Optional unwrapKey() {
         return Optional.of(this.key);
      }

      public boolean contains(final Holder value) {
         return value.is(this.key);
      }

      public String toString() {
         String var10000 = String.valueOf(this.key);
         return "NamedSet(" + var10000 + ")[" + String.valueOf(this.contents) + "]";
      }

      public boolean canSerializeIn(final HolderOwner context) {
         return this.owner.canSerializeIn(context);
      }
   }
}
