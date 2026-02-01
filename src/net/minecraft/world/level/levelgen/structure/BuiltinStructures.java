package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface BuiltinStructures {
   ResourceKey PILLAGER_OUTPOST = createKey("pillager_outpost");
   ResourceKey MINESHAFT = createKey("mineshaft");
   ResourceKey MINESHAFT_MESA = createKey("mineshaft_mesa");
   ResourceKey WOODLAND_MANSION = createKey("mansion");
   ResourceKey JUNGLE_TEMPLE = createKey("jungle_pyramid");
   ResourceKey DESERT_PYRAMID = createKey("desert_pyramid");
   ResourceKey IGLOO = createKey("igloo");
   ResourceKey SHIPWRECK = createKey("shipwreck");
   ResourceKey SHIPWRECK_BEACHED = createKey("shipwreck_beached");
   ResourceKey SWAMP_HUT = createKey("swamp_hut");
   ResourceKey STRONGHOLD = createKey("stronghold");
   ResourceKey OCEAN_MONUMENT = createKey("monument");
   ResourceKey OCEAN_RUIN_COLD = createKey("ocean_ruin_cold");
   ResourceKey OCEAN_RUIN_WARM = createKey("ocean_ruin_warm");
   ResourceKey FORTRESS = createKey("fortress");
   ResourceKey NETHER_FOSSIL = createKey("nether_fossil");
   ResourceKey END_CITY = createKey("end_city");
   ResourceKey BURIED_TREASURE = createKey("buried_treasure");
   ResourceKey BASTION_REMNANT = createKey("bastion_remnant");
   ResourceKey VILLAGE_PLAINS = createKey("village_plains");
   ResourceKey VILLAGE_DESERT = createKey("village_desert");
   ResourceKey VILLAGE_SAVANNA = createKey("village_savanna");
   ResourceKey VILLAGE_SNOWY = createKey("village_snowy");
   ResourceKey VILLAGE_TAIGA = createKey("village_taiga");
   ResourceKey RUINED_PORTAL_STANDARD = createKey("ruined_portal");
   ResourceKey RUINED_PORTAL_DESERT = createKey("ruined_portal_desert");
   ResourceKey RUINED_PORTAL_JUNGLE = createKey("ruined_portal_jungle");
   ResourceKey RUINED_PORTAL_SWAMP = createKey("ruined_portal_swamp");
   ResourceKey RUINED_PORTAL_MOUNTAIN = createKey("ruined_portal_mountain");
   ResourceKey RUINED_PORTAL_OCEAN = createKey("ruined_portal_ocean");
   ResourceKey RUINED_PORTAL_NETHER = createKey("ruined_portal_nether");
   ResourceKey ANCIENT_CITY = createKey("ancient_city");
   ResourceKey TRAIL_RUINS = createKey("trail_ruins");
   ResourceKey TRIAL_CHAMBERS = createKey("trial_chambers");

   private static ResourceKey createKey(final String name) {
      return ResourceKey.create(Registries.STRUCTURE, Identifier.withDefaultNamespace(name));
   }
}
