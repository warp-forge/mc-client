package net.minecraft.world.entity.variant;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public interface PriorityProvider {
   List selectors();

   static Stream select(final Stream entries, final Function extractor, final Object context) {
      List<UnpackedEntry<C, T>> unpackedEntries = new ArrayList();
      entries.forEach((entryx) -> {
         PriorityProvider<C, ?> provider = (PriorityProvider)extractor.apply(entryx);

         for(Selector selector : provider.selectors()) {
            unpackedEntries.add(new UnpackedEntry(entryx, selector.priority(), (SelectorCondition)DataFixUtils.orElseGet(selector.condition(), SelectorCondition::alwaysTrue)));
         }

      });
      unpackedEntries.sort(PriorityProvider.UnpackedEntry.HIGHEST_PRIORITY_FIRST);
      Iterator<UnpackedEntry<C, T>> iterator = unpackedEntries.iterator();
      int highestMatchedPriority = Integer.MIN_VALUE;

      while(iterator.hasNext()) {
         UnpackedEntry<C, T> entry = (UnpackedEntry)iterator.next();
         if (entry.priority < highestMatchedPriority) {
            iterator.remove();
         } else if (entry.condition.test(context)) {
            highestMatchedPriority = entry.priority;
         } else {
            iterator.remove();
         }
      }

      return unpackedEntries.stream().map(UnpackedEntry::entry);
   }

   static Optional pick(final Stream entries, final Function extractor, final RandomSource randomSource, final Object context) {
      List<T> selected = select(entries, extractor, context).toList();
      return Util.getRandomSafe(selected, randomSource);
   }

   static List single(final SelectorCondition check, final int priority) {
      return List.of(new Selector(check, priority));
   }

   static List alwaysTrue(final int priority) {
      return List.of(new Selector(Optional.empty(), priority));
   }

   public static record Selector(Optional condition, int priority) {
      public Selector(final SelectorCondition condition, final int priority) {
         this(Optional.of(condition), priority);
      }

      public Selector(final int priority) {
         this(Optional.empty(), priority);
      }

      public static Codec codec(final Codec conditionCodec) {
         return RecordCodecBuilder.create((i) -> i.group(conditionCodec.optionalFieldOf("condition").forGetter(Selector::condition), Codec.INT.fieldOf("priority").forGetter(Selector::priority)).apply(i, Selector::new));
      }
   }

   @FunctionalInterface
   public interface SelectorCondition extends Predicate {
      static SelectorCondition alwaysTrue() {
         return (context) -> true;
      }
   }

   public static record UnpackedEntry(Object entry, int priority, SelectorCondition condition) {
      public static final Comparator HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(UnpackedEntry::priority).reversed();
   }
}
