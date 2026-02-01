package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.IdentifierParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {
   public static Grammar createGrammar(final Context context) {
      Atom<List<T>> top = Atom.of("top");
      Atom<Optional<T>> type = Atom.of("type");
      Atom<Unit> anyType = Atom.of("any_type");
      Atom<T> elementType = Atom.of("element_type");
      Atom<T> tagType = Atom.of("tag_type");
      Atom<List<T>> conditions = Atom.of("conditions");
      Atom<List<T>> alternatives = Atom.of("alternatives");
      Atom<T> term = Atom.of("term");
      Atom<T> negation = Atom.of("negation");
      Atom<T> test = Atom.of("test");
      Atom<C> componentType = Atom.of("component_type");
      Atom<P> predicateType = Atom.of("predicate_type");
      Atom<Identifier> id = Atom.of("id");
      Atom<Dynamic<?>> tag = Atom.of("tag");
      Dictionary<StringReader> rules = new Dictionary();
      NamedRule<StringReader, Identifier> idRule = rules.put(id, IdentifierParseRule.INSTANCE);
      NamedRule<StringReader, List<T>> topRule = rules.put(top, Term.alternative(Term.sequence(rules.named(type), StringReaderTerms.character('['), Term.cut(), Term.optional(rules.named(conditions)), StringReaderTerms.character(']')), rules.named(type)), (scope) -> {
         ImmutableList.Builder<T> builder = ImmutableList.builder();
         Optional var10000 = (Optional)scope.getOrThrow(type);
         Objects.requireNonNull(builder);
         var10000.ifPresent(builder::add);
         List<T> parsedConditions = (List)scope.get(conditions);
         if (parsedConditions != null) {
            builder.addAll(parsedConditions);
         }

         return builder.build();
      });
      rules.put(type, Term.alternative(rules.named(elementType), Term.sequence(StringReaderTerms.character('#'), Term.cut(), rules.named(tagType)), rules.named(anyType)), (scope) -> Optional.ofNullable(scope.getAny(elementType, tagType)));
      rules.put(anyType, StringReaderTerms.character('*'), (s) -> Unit.INSTANCE);
      rules.put(elementType, new ElementLookupRule(idRule, context));
      rules.put(tagType, new TagLookupRule(idRule, context));
      rules.put(conditions, Term.sequence(rules.named(alternatives), Term.optional(Term.sequence(StringReaderTerms.character(','), rules.named(conditions)))), (scope) -> {
         T parsedCondition = (T)context.anyOf((List)scope.getOrThrow(alternatives));
         return (List)Optional.ofNullable((List)scope.get(conditions)).map((rest) -> Util.copyAndAdd(parsedCondition, rest)).orElse(List.of(parsedCondition));
      });
      rules.put(alternatives, Term.sequence(rules.named(term), Term.optional(Term.sequence(StringReaderTerms.character('|'), rules.named(alternatives)))), (scope) -> {
         T alternative = (T)scope.getOrThrow(term);
         return (List)Optional.ofNullable((List)scope.get(alternatives)).map((rest) -> Util.copyAndAdd(alternative, rest)).orElse(List.of(alternative));
      });
      rules.put(term, Term.alternative(rules.named(test), Term.sequence(StringReaderTerms.character('!'), rules.named(negation))), (scope) -> scope.getAnyOrThrow(test, negation));
      rules.put(negation, rules.named(test), (scope) -> context.negate(scope.getOrThrow(test)));
      rules.putComplex(test, Term.alternative(Term.sequence(rules.named(componentType), StringReaderTerms.character('='), Term.cut(), rules.named(tag)), Term.sequence(rules.named(predicateType), StringReaderTerms.character('~'), Term.cut(), rules.named(tag)), rules.named(componentType)), (state) -> {
         Scope scope = state.scope();
         P predicate = (P)scope.get(predicateType);

         try {
            if (predicate != null) {
               Dynamic<?> value = (Dynamic)scope.getOrThrow(tag);
               return context.createPredicateTest((ImmutableStringReader)state.input(), predicate, value);
            } else {
               C component = (C)scope.getOrThrow(componentType);
               Dynamic<?> value = (Dynamic)scope.get(tag);
               return value != null ? context.createComponentTest((ImmutableStringReader)state.input(), component, value) : context.createComponentTest((ImmutableStringReader)state.input(), component);
            }
         } catch (CommandSyntaxException e) {
            state.errorCollector().store(state.mark(), e);
            return null;
         }
      });
      rules.put(componentType, new ComponentLookupRule(idRule, context));
      rules.put(predicateType, new PredicateLookupRule(idRule, context));
      rules.put(tag, new TagParseRule(NbtOps.INSTANCE));
      return new Grammar(rules, topRule);
   }

   private static class ElementLookupRule extends ResourceLookupRule {
      private ElementLookupRule(final NamedRule idParser, final Context context) {
         super(idParser, context);
      }

      protected Object validateElement(final ImmutableStringReader reader, final Identifier id) throws Exception {
         return ((Context)this.context).forElementType(reader, id);
      }

      public Stream possibleResources() {
         return ((Context)this.context).listElementTypes();
      }
   }

   private static class TagLookupRule extends ResourceLookupRule {
      private TagLookupRule(final NamedRule idParser, final Context context) {
         super(idParser, context);
      }

      protected Object validateElement(final ImmutableStringReader reader, final Identifier id) throws Exception {
         return ((Context)this.context).forTagType(reader, id);
      }

      public Stream possibleResources() {
         return ((Context)this.context).listTagTypes();
      }
   }

   private static class ComponentLookupRule extends ResourceLookupRule {
      private ComponentLookupRule(final NamedRule idParser, final Context context) {
         super(idParser, context);
      }

      protected Object validateElement(final ImmutableStringReader reader, final Identifier id) throws Exception {
         return ((Context)this.context).lookupComponentType(reader, id);
      }

      public Stream possibleResources() {
         return ((Context)this.context).listComponentTypes();
      }
   }

   private static class PredicateLookupRule extends ResourceLookupRule {
      private PredicateLookupRule(final NamedRule idParser, final Context context) {
         super(idParser, context);
      }

      protected Object validateElement(final ImmutableStringReader reader, final Identifier id) throws Exception {
         return ((Context)this.context).lookupPredicateType(reader, id);
      }

      public Stream possibleResources() {
         return ((Context)this.context).listPredicateTypes();
      }
   }

   public interface Context {
      Object forElementType(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException;

      Stream listElementTypes();

      Object forTagType(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException;

      Stream listTagTypes();

      Object lookupComponentType(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException;

      Stream listComponentTypes();

      Object createComponentTest(ImmutableStringReader reader, Object componentType, Dynamic value) throws CommandSyntaxException;

      Object createComponentTest(ImmutableStringReader reader, Object componentType);

      Object lookupPredicateType(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException;

      Stream listPredicateTypes();

      Object createPredicateTest(ImmutableStringReader reader, Object predicateType, Dynamic value) throws CommandSyntaxException;

      Object negate(Object value);

      Object anyOf(List alternatives);
   }
}
