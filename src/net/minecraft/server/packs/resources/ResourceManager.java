package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

public interface ResourceManager extends ResourceProvider {
   Set getNamespaces();

   List getResourceStack(Identifier location);

   Map listResources(String directory, Predicate filter);

   Map listResourceStacks(String directory, Predicate filter);

   Stream listPacks();

   public static enum Empty implements ResourceManager {
      INSTANCE;

      public Set getNamespaces() {
         return Set.of();
      }

      public Optional getResource(final Identifier location) {
         return Optional.empty();
      }

      public List getResourceStack(final Identifier location) {
         return List.of();
      }

      public Map listResources(final String directory, final Predicate filter) {
         return Map.of();
      }

      public Map listResourceStacks(final String directory, final Predicate filter) {
         return Map.of();
      }

      public Stream listPacks() {
         return Stream.of();
      }

      // $FF: synthetic method
      private static Empty[] $values() {
         return new Empty[]{INSTANCE};
      }
   }
}
