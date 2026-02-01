package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList implements Iterable {
   protected final List entries;
   private final RandomSource random = RandomSource.create();

   public ShufflingList() {
      this.entries = Lists.newArrayList();
   }

   private ShufflingList(final List entries) {
      this.entries = Lists.newArrayList(entries);
   }

   public static Codec codec(final Codec elementCodec) {
      return ShufflingList.WeightedEntry.codec(elementCodec).listOf().xmap(ShufflingList::new, (l) -> l.entries);
   }

   public ShufflingList add(final Object data, final int weight) {
      this.entries.add(new WeightedEntry(data, weight));
      return this;
   }

   public ShufflingList shuffle() {
      this.entries.forEach((k) -> k.setRandom(this.random.nextFloat()));
      this.entries.sort(Comparator.comparingDouble(WeightedEntry::getRandWeight));
      return this;
   }

   public Stream stream() {
      return this.entries.stream().map(WeightedEntry::getData);
   }

   public Iterator iterator() {
      return Iterators.transform(this.entries.iterator(), WeightedEntry::getData);
   }

   public String toString() {
      return "ShufflingList[" + String.valueOf(this.entries) + "]";
   }

   public static class WeightedEntry {
      private final Object data;
      private final int weight;
      private double randWeight;

      private WeightedEntry(final Object data, final int weight) {
         this.weight = weight;
         this.data = data;
      }

      private double getRandWeight() {
         return this.randWeight;
      }

      private void setRandom(final float random) {
         this.randWeight = -Math.pow((double)random, (double)(1.0F / (float)this.weight));
      }

      public Object getData() {
         return this.data;
      }

      public int getWeight() {
         return this.weight;
      }

      public String toString() {
         int var10000 = this.weight;
         return var10000 + ":" + String.valueOf(this.data);
      }

      public static Codec codec(final Codec elementCodec) {
         return new Codec() {
            public DataResult decode(final DynamicOps ops, final Object input) {
               Dynamic<T> map = new Dynamic(ops, input);
               OptionalDynamic var10000 = map.get("data");
               Codec var10001 = elementCodec;
               Objects.requireNonNull(var10001);
               return var10000.flatMap(var10001::parse).map((data) -> new WeightedEntry(data, map.get("weight").asInt(1))).map((r) -> Pair.of(r, ops.empty()));
            }

            public DataResult encode(final WeightedEntry input, final DynamicOps ops, final Object prefix) {
               return ops.mapBuilder().add("weight", ops.createInt(input.weight)).add("data", elementCodec.encodeStart(ops, input.data)).build(prefix);
            }
         };
      }
   }
}
