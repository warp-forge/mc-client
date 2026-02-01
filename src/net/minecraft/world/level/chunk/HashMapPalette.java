package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette implements Palette {
   private final CrudeIncrementalIntIdentityHashBiMap values;
   private final int bits;

   public HashMapPalette(final int bits, final List values) {
      this(bits);
      CrudeIncrementalIntIdentityHashBiMap var10001 = this.values;
      Objects.requireNonNull(var10001);
      values.forEach(var10001::add);
   }

   public HashMapPalette(final int bits) {
      this(bits, CrudeIncrementalIntIdentityHashBiMap.create(1 << bits));
   }

   private HashMapPalette(final int bits, final CrudeIncrementalIntIdentityHashBiMap values) {
      this.bits = bits;
      this.values = values;
   }

   public static Palette create(final int bits, final List paletteEntries) {
      return new HashMapPalette(bits, paletteEntries);
   }

   public int idFor(final Object value, final PaletteResize resizeHandler) {
      int id = this.values.getId(value);
      if (id == -1) {
         id = this.values.add(value);
         if (id >= 1 << this.bits) {
            id = resizeHandler.onResize(this.bits + 1, value);
         }
      }

      return id;
   }

   public boolean maybeHas(final Predicate predicate) {
      for(int i = 0; i < this.getSize(); ++i) {
         if (predicate.test(this.values.byId(i))) {
            return true;
         }
      }

      return false;
   }

   public Object valueFor(final int index) {
      T value = (T)this.values.byId(index);
      if (value == null) {
         throw new MissingPaletteEntryException(index);
      } else {
         return value;
      }
   }

   public void read(final FriendlyByteBuf buffer, final IdMap globalMap) {
      this.values.clear();
      int size = buffer.readVarInt();

      for(int i = 0; i < size; ++i) {
         this.values.add(globalMap.byIdOrThrow(buffer.readVarInt()));
      }

   }

   public void write(final FriendlyByteBuf buffer, final IdMap globalMap) {
      int size = this.getSize();
      buffer.writeVarInt(size);

      for(int i = 0; i < size; ++i) {
         buffer.writeVarInt(globalMap.getId(this.values.byId(i)));
      }

   }

   public int getSerializedSize(final IdMap globalMap) {
      int size = VarInt.getByteSize(this.getSize());

      for(int i = 0; i < this.getSize(); ++i) {
         size += VarInt.getByteSize(globalMap.getId(this.values.byId(i)));
      }

      return size;
   }

   public List getEntries() {
      ArrayList<T> list = new ArrayList();
      Iterator var10000 = this.values.iterator();
      Objects.requireNonNull(list);
      var10000.forEachRemaining(list::add);
      return list;
   }

   public int getSize() {
      return this.values.size();
   }

   public Palette copy() {
      return new HashMapPalette(this.bits, this.values.copy());
   }
}
