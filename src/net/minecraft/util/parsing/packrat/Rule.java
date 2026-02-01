package net.minecraft.util.parsing.packrat;

import org.jspecify.annotations.Nullable;

public interface Rule {
   @Nullable Object parse(ParseState state);

   static Rule fromTerm(final Term child, final RuleAction action) {
      return new WrappedTerm(action, child);
   }

   static Rule fromTerm(final Term child, final SimpleRuleAction action) {
      return new WrappedTerm(action, child);
   }

   @FunctionalInterface
   public interface SimpleRuleAction extends RuleAction {
      Object run(Scope ruleScope);

      default Object run(final ParseState state) {
         return this.run(state.scope());
      }
   }

   public static record WrappedTerm(RuleAction action, Term child) implements Rule {
      public @Nullable Object parse(final ParseState state) {
         Scope scope = state.scope();
         scope.pushFrame();

         Object var3;
         try {
            if (!this.child.parse(state, scope, Control.UNBOUND)) {
               var3 = null;
               return var3;
            }

            var3 = this.action.run(state);
         } finally {
            scope.popFrame();
         }

         return var3;
      }
   }

   @FunctionalInterface
   public interface RuleAction {
      @Nullable Object run(ParseState state);
   }
}
