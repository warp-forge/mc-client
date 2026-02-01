package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.profiling.jfr.Percentiles;
import org.jspecify.annotations.Nullable;

public record TimedStatSummary(TimedStat fastest, TimedStat slowest, @Nullable TimedStat secondSlowest, int count, Map percentilesNanos, Duration totalDuration) {
   public static Optional summary(final List values) {
      if (values.isEmpty()) {
         return Optional.empty();
      } else {
         List<T> sorted = values.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
         Duration totalDuration = (Duration)sorted.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
         T fastest = (T)((TimedStat)sorted.getFirst());
         T slowest = (T)((TimedStat)sorted.getLast());
         T secondSlowest = (T)(sorted.size() > 1 ? (TimedStat)sorted.get(sorted.size() - 2) : null);
         int count = sorted.size();
         Map<Integer, Double> percentilesNanos = Percentiles.evaluate(sorted.stream().mapToLong((it) -> it.duration().toNanos()).toArray());
         return Optional.of(new TimedStatSummary(fastest, slowest, secondSlowest, count, percentilesNanos, totalDuration));
      }
   }
}
