package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D {
   private final int minX;
   private final int minZ;
   private final int sizeX;
   private final int sizeZ;
   private final Object[] cache;

   public static StaticCache2D create(final int centerX, final int centerZ, final int range, final Initializer initializer) {
      int minX = centerX - range;
      int minZ = centerZ - range;
      int size = 2 * range + 1;
      return new StaticCache2D(minX, minZ, size, size, initializer);
   }

   private StaticCache2D(final int minX, final int minZ, final int sizeX, final int sizeZ, final Initializer initializer) {
      this.minX = minX;
      this.minZ = minZ;
      this.sizeX = sizeX;
      this.sizeZ = sizeZ;
      this.cache = new Object[this.sizeX * this.sizeZ];

      for(int x = minX; x < minX + sizeX; ++x) {
         for(int z = minZ; z < minZ + sizeZ; ++z) {
            this.cache[this.getIndex(x, z)] = initializer.get(x, z);
         }
      }

   }

   public void forEach(final Consumer consumer) {
      for(Object o : this.cache) {
         consumer.accept(o);
      }

   }

   public Object get(final int x, final int z) {
      if (!this.contains(x, z)) {
         throw new IllegalArgumentException("Requested out of range value (" + x + "," + z + ") from " + String.valueOf(this));
      } else {
         return this.cache[this.getIndex(x, z)];
      }
   }

   public boolean contains(final int x, final int z) {
      int deltaX = x - this.minX;
      int deltaZ = z - this.minZ;
      return deltaX >= 0 && deltaX < this.sizeX && deltaZ >= 0 && deltaZ < this.sizeZ;
   }

   public String toString() {
      return String.format(Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ);
   }

   private int getIndex(final int x, final int z) {
      int deltaX = x - this.minX;
      int deltaZ = z - this.minZ;
      return deltaX * this.sizeZ + deltaZ;
   }

   @FunctionalInterface
   public interface Initializer {
      Object get(int x, int z);
   }
}
