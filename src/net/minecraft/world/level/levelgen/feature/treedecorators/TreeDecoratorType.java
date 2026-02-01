package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class TreeDecoratorType {
   public static final TreeDecoratorType TRUNK_VINE;
   public static final TreeDecoratorType LEAVE_VINE;
   public static final TreeDecoratorType PALE_MOSS;
   public static final TreeDecoratorType CREAKING_HEART;
   public static final TreeDecoratorType COCOA;
   public static final TreeDecoratorType BEEHIVE;
   public static final TreeDecoratorType ALTER_GROUND;
   public static final TreeDecoratorType ATTACHED_TO_LEAVES;
   public static final TreeDecoratorType PLACE_ON_GROUND;
   public static final TreeDecoratorType ATTACHED_TO_LOGS;
   private final MapCodec codec;

   private static TreeDecoratorType register(final String name, final MapCodec codec) {
      return (TreeDecoratorType)Registry.register(BuiltInRegistries.TREE_DECORATOR_TYPE, (String)name, new TreeDecoratorType(codec));
   }

   private TreeDecoratorType(final MapCodec codec) {
      this.codec = codec;
   }

   public MapCodec codec() {
      return this.codec;
   }

   static {
      TRUNK_VINE = register("trunk_vine", TrunkVineDecorator.CODEC);
      LEAVE_VINE = register("leave_vine", LeaveVineDecorator.CODEC);
      PALE_MOSS = register("pale_moss", PaleMossDecorator.CODEC);
      CREAKING_HEART = register("creaking_heart", CreakingHeartDecorator.CODEC);
      COCOA = register("cocoa", CocoaDecorator.CODEC);
      BEEHIVE = register("beehive", BeehiveDecorator.CODEC);
      ALTER_GROUND = register("alter_ground", AlterGroundDecorator.CODEC);
      ATTACHED_TO_LEAVES = register("attached_to_leaves", AttachedToLeavesDecorator.CODEC);
      PLACE_ON_GROUND = register("place_on_ground", PlaceOnGroundDecorator.CODEC);
      ATTACHED_TO_LOGS = register("attached_to_logs", AttachedToLogsDecorator.CODEC);
   }
}
