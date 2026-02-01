package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public interface StructureTags {
   TagKey EYE_OF_ENDER_LOCATED = create("eye_of_ender_located");
   TagKey DOLPHIN_LOCATED = create("dolphin_located");
   TagKey ON_WOODLAND_EXPLORER_MAPS = create("on_woodland_explorer_maps");
   TagKey ON_OCEAN_EXPLORER_MAPS = create("on_ocean_explorer_maps");
   TagKey ON_SAVANNA_VILLAGE_MAPS = create("on_savanna_village_maps");
   TagKey ON_DESERT_VILLAGE_MAPS = create("on_desert_village_maps");
   TagKey ON_PLAINS_VILLAGE_MAPS = create("on_plains_village_maps");
   TagKey ON_TAIGA_VILLAGE_MAPS = create("on_taiga_village_maps");
   TagKey ON_SNOWY_VILLAGE_MAPS = create("on_snowy_village_maps");
   TagKey ON_JUNGLE_EXPLORER_MAPS = create("on_jungle_explorer_maps");
   TagKey ON_SWAMP_EXPLORER_MAPS = create("on_swamp_explorer_maps");
   TagKey ON_TREASURE_MAPS = create("on_treasure_maps");
   TagKey ON_TRIAL_CHAMBERS_MAPS = create("on_trial_chambers_maps");
   TagKey CATS_SPAWN_IN = create("cats_spawn_in");
   TagKey CATS_SPAWN_AS_BLACK = create("cats_spawn_as_black");
   TagKey VILLAGE = create("village");
   TagKey MINESHAFT = create("mineshaft");
   TagKey SHIPWRECK = create("shipwreck");
   TagKey RUINED_PORTAL = create("ruined_portal");
   TagKey OCEAN_RUIN = create("ocean_ruin");

   private static TagKey create(final String name) {
      return TagKey.create(Registries.STRUCTURE, Identifier.withDefaultNamespace(name));
   }
}
