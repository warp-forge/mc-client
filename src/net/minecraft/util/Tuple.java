package net.minecraft.util;

public class Tuple {
   private Object a;
   private Object b;

   public Tuple(final Object a, final Object b) {
      this.a = a;
      this.b = b;
   }

   public Object getA() {
      return this.a;
   }

   public void setA(final Object a) {
      this.a = a;
   }

   public Object getB() {
      return this.b;
   }

   public void setB(final Object b) {
      this.b = b;
   }
}
