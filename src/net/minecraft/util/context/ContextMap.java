package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class ContextMap {
   private final Map params;

   private ContextMap(final Map params) {
      this.params = params;
   }

   public boolean has(final ContextKey key) {
      return this.params.containsKey(key);
   }

   public Object getOrThrow(final ContextKey key) {
      T value = (T)this.params.get(key);
      if (value == null) {
         throw new NoSuchElementException(key.name().toString());
      } else {
         return value;
      }
   }

   public @Nullable Object getOptional(final ContextKey key) {
      return this.params.get(key);
   }

   @Contract("_,!null->!null; _,_->_")
   public @Nullable Object getOrDefault(final ContextKey param, final @Nullable Object _default) {
      return this.params.getOrDefault(param, _default);
   }

   public static class Builder {
      private final Map params = new IdentityHashMap();

      public Builder withParameter(final ContextKey param, final Object value) {
         this.params.put(param, value);
         return this;
      }

      public Builder withOptionalParameter(final ContextKey param, final @Nullable Object value) {
         if (value == null) {
            this.params.remove(param);
         } else {
            this.params.put(param, value);
         }

         return this;
      }

      public Object getParameter(final ContextKey param) {
         T value = (T)this.params.get(param);
         if (value == null) {
            throw new NoSuchElementException(param.name().toString());
         } else {
            return value;
         }
      }

      public @Nullable Object getOptionalParameter(final ContextKey param) {
         return this.params.get(param);
      }

      public ContextMap create(final ContextKeySet paramSet) {
         Set<ContextKey<?>> notAllowed = Sets.difference(this.params.keySet(), paramSet.allowed());
         if (!notAllowed.isEmpty()) {
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(notAllowed));
         } else {
            Set<ContextKey<?>> missingRequired = Sets.difference(paramSet.required(), this.params.keySet());
            if (!missingRequired.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(missingRequired));
            } else {
               return new ContextMap(this.params);
            }
         }
      }
   }
}
