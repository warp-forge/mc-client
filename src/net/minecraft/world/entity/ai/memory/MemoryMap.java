package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;

public final class MemoryMap implements Iterable {
   private static final Codec SERIALIZABLE_MEMORY_MODULE_CODEC;
   public static final Codec CODEC;
   public static final MemoryMap EMPTY;
   private final Map memories;

   private MemoryMap(final Map memories) {
      this.memories = Map.copyOf(memories);
   }

   public static MemoryMap of(final Stream memories) {
      return new MemoryMap((Map)memories.collect(Collectors.toMap(Value::type, Value::value)));
   }

   public @Nullable ExpirableValue get(final MemoryModuleType type) {
      return (ExpirableValue)this.memories.get(type);
   }

   public boolean equals(final Object obj) {
      boolean var10000;
      if (obj instanceof MemoryMap map) {
         if (this.memories.equals(map.memories)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public int hashCode() {
      return this.memories.hashCode();
   }

   public String toString() {
      return this.memories.toString();
   }

   public Iterator iterator() {
      return Iterators.transform(this.memories.entrySet().iterator(), (entry) -> MemoryMap.Value.createUnchecked((MemoryModuleType)entry.getKey(), (ExpirableValue)entry.getValue()));
   }

   static {
      SERIALIZABLE_MEMORY_MODULE_CODEC = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().validate((type) -> type.getCodec().isPresent() ? DataResult.success(type) : DataResult.error(() -> "Memory module " + String.valueOf(type) + " cannot be encoded"));
      CODEC = Codec.dispatchedMap(SERIALIZABLE_MEMORY_MODULE_CODEC, (type) -> (Codec)type.getCodec().orElseThrow()).xmap(MemoryMap::new, (m) -> m.memories);
      EMPTY = new MemoryMap(Map.of());
   }

   public static record Value(MemoryModuleType type, ExpirableValue value) {
      public static Value createUnchecked(final MemoryModuleType type, final ExpirableValue value) {
         return new Value(type, value);
      }
   }
}
