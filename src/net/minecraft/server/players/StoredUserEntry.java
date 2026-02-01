package net.minecraft.server.players;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public abstract class StoredUserEntry {
   private final @Nullable Object user;

   public StoredUserEntry(final @Nullable Object user) {
      this.user = user;
   }

   public @Nullable Object getUser() {
      return this.user;
   }

   boolean hasExpired() {
      return false;
   }

   protected abstract void serialize(final JsonObject object);
}
