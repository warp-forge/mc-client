package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface RuleTestType {
   RuleTestType ALWAYS_TRUE_TEST = register("always_true", AlwaysTrueTest.CODEC);
   RuleTestType BLOCK_TEST = register("block_match", BlockMatchTest.CODEC);
   RuleTestType BLOCKSTATE_TEST = register("blockstate_match", BlockStateMatchTest.CODEC);
   RuleTestType TAG_TEST = register("tag_match", TagMatchTest.CODEC);
   RuleTestType RANDOM_BLOCK_TEST = register("random_block_match", RandomBlockMatchTest.CODEC);
   RuleTestType RANDOM_BLOCKSTATE_TEST = register("random_blockstate_match", RandomBlockStateMatchTest.CODEC);

   MapCodec codec();

   static RuleTestType register(final String id, final MapCodec codec) {
      return (RuleTestType)Registry.register(BuiltInRegistries.RULE_TEST, (String)id, (RuleTestType)() -> codec);
   }
}
