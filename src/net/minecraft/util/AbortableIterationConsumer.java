package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer {
   Continuation accept(Object entry);

   static AbortableIterationConsumer forConsumer(final Consumer consumer) {
      return (e) -> {
         consumer.accept(e);
         return AbortableIterationConsumer.Continuation.CONTINUE;
      };
   }

   public static enum Continuation {
      CONTINUE,
      ABORT;

      public boolean shouldAbort() {
         return this == ABORT;
      }

      // $FF: synthetic method
      private static Continuation[] $values() {
         return new Continuation[]{CONTINUE, ABORT};
      }
   }
}
