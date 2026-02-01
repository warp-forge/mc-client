package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;

public interface Validatable {
   void validate(ValidationContext context);

   static void validate(final ValidationContext context, final String name, final Validatable v) {
      v.validate(context.forField(name));
   }

   static void validate(final ValidationContext context, final String name, final Optional optional) {
      optional.ifPresent((v) -> v.validate(context.forField(name)));
   }

   static void validate(final ValidationContext context, final String name, final List list) {
      for(int i = 0; i < list.size(); ++i) {
         ((Validatable)list.get(i)).validate(context.forIndexedField(name, i));
      }

   }

   static void validate(final ValidationContext context, final List list) {
      for(int i = 0; i < list.size(); ++i) {
         ((Validatable)list.get(i)).validate(context.forChild(new ProblemReporter.IndexedPathElement(i)));
      }

   }

   static void validateReference(final ValidationContext context, final ResourceKey id) {
      if (!context.allowsReferences()) {
         context.reportProblem(new ValidationContext.ReferenceNotAllowedProblem(id));
      } else if (context.hasVisitedElement(id)) {
         context.reportProblem(new ValidationContext.RecursiveReferenceProblem(id));
      } else {
         context.resolver().get(id).ifPresentOrElse((element) -> ((Validatable)element.value()).validate(context.enterElement(new ProblemReporter.ElementReferencePathElement(id), id)), () -> context.reportProblem(new ValidationContext.MissingReferenceProblem(id)));
      }
   }

   static Function validatorForContext(final ContextKeySet params) {
      return (v) -> {
         ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
         ValidationContext validationContext = new ValidationContext(problemCollector, params);
         v.validate(validationContext);
         return !problemCollector.isEmpty() ? DataResult.error(() -> "Validation error: " + problemCollector.getReport()) : DataResult.success(v);
      };
   }

   static Function listValidatorForContext(final ContextKeySet params) {
      return (v) -> {
         ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
         ValidationContext validationContext = new ValidationContext(problemCollector, params);
         validate(validationContext, v);
         return !problemCollector.isEmpty() ? DataResult.error(() -> "Validation error: " + problemCollector.getReport()) : DataResult.success(v);
      };
   }
}
