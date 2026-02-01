package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class DataComponentPredicates {
   public static final DataComponentPredicate.Type DAMAGE;
   public static final DataComponentPredicate.Type ENCHANTMENTS;
   public static final DataComponentPredicate.Type STORED_ENCHANTMENTS;
   public static final DataComponentPredicate.Type POTIONS;
   public static final DataComponentPredicate.Type CUSTOM_DATA;
   public static final DataComponentPredicate.Type CONTAINER;
   public static final DataComponentPredicate.Type BUNDLE_CONTENTS;
   public static final DataComponentPredicate.Type FIREWORK_EXPLOSION;
   public static final DataComponentPredicate.Type FIREWORKS;
   public static final DataComponentPredicate.Type WRITABLE_BOOK;
   public static final DataComponentPredicate.Type WRITTEN_BOOK;
   public static final DataComponentPredicate.Type ATTRIBUTE_MODIFIERS;
   public static final DataComponentPredicate.Type ARMOR_TRIM;
   public static final DataComponentPredicate.Type JUKEBOX_PLAYABLE;
   public static final DataComponentPredicate.Type VILLAGER_VARIANT;

   private static DataComponentPredicate.Type register(final String id, final Codec codec) {
      return (DataComponentPredicate.Type)Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, (String)id, new DataComponentPredicate.ConcreteType(codec));
   }

   public static DataComponentPredicate.Type bootstrap(final Registry registry) {
      return DAMAGE;
   }

   static {
      DAMAGE = register("damage", DamagePredicate.CODEC);
      ENCHANTMENTS = register("enchantments", EnchantmentsPredicate.Enchantments.CODEC);
      STORED_ENCHANTMENTS = register("stored_enchantments", EnchantmentsPredicate.StoredEnchantments.CODEC);
      POTIONS = register("potion_contents", PotionsPredicate.CODEC);
      CUSTOM_DATA = register("custom_data", CustomDataPredicate.CODEC);
      CONTAINER = register("container", ContainerPredicate.CODEC);
      BUNDLE_CONTENTS = register("bundle_contents", BundlePredicate.CODEC);
      FIREWORK_EXPLOSION = register("firework_explosion", FireworkExplosionPredicate.CODEC);
      FIREWORKS = register("fireworks", FireworksPredicate.CODEC);
      WRITABLE_BOOK = register("writable_book_content", WritableBookPredicate.CODEC);
      WRITTEN_BOOK = register("written_book_content", WrittenBookPredicate.CODEC);
      ATTRIBUTE_MODIFIERS = register("attribute_modifiers", AttributeModifiersPredicate.CODEC);
      ARMOR_TRIM = register("trim", TrimPredicate.CODEC);
      JUKEBOX_PLAYABLE = register("jukebox_playable", JukeboxPlayablePredicate.CODEC);
      VILLAGER_VARIANT = register("villager/variant", VillagerTypePredicate.CODEC);
   }
}
