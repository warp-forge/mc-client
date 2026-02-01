package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;

public interface StructureProcessorType {
   Codec SINGLE_CODEC = BuiltInRegistries.STRUCTURE_PROCESSOR.byNameCodec().dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
   Codec LIST_OBJECT_CODEC = SINGLE_CODEC.listOf().xmap(StructureProcessorList::new, StructureProcessorList::list);
   Codec DIRECT_CODEC = Codec.withAlternative(LIST_OBJECT_CODEC.fieldOf("processors").codec(), LIST_OBJECT_CODEC);
   Codec LIST_CODEC = RegistryFileCodec.create(Registries.PROCESSOR_LIST, DIRECT_CODEC);
   StructureProcessorType BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
   StructureProcessorType BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
   StructureProcessorType GRAVITY = register("gravity", GravityProcessor.CODEC);
   StructureProcessorType JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
   StructureProcessorType RULE = register("rule", RuleProcessor.CODEC);
   StructureProcessorType NOP = register("nop", NopProcessor.CODEC);
   StructureProcessorType BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
   StructureProcessorType BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
   StructureProcessorType LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
   StructureProcessorType PROTECTED_BLOCKS = register("protected_blocks", ProtectedBlockProcessor.CODEC);
   StructureProcessorType CAPPED = register("capped", CappedProcessor.CODEC);

   MapCodec codec();

   static StructureProcessorType register(final String id, final MapCodec codec) {
      return (StructureProcessorType)Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, (String)id, (StructureProcessorType)() -> codec);
   }
}
