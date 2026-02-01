package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Set;

public record RealmsWorldResetDto(String seed, long worldTemplateId, int levelType, boolean generateStructures, Set experiments) implements ReflectionBasedSerialization {
   @SerializedName("seed")
   public String seed() {
      return this.seed;
   }

   @SerializedName("worldTemplateId")
   public long worldTemplateId() {
      return this.worldTemplateId;
   }

   @SerializedName("levelType")
   public int levelType() {
      return this.levelType;
   }

   @SerializedName("generateStructures")
   public boolean generateStructures() {
      return this.generateStructures;
   }

   @SerializedName("experiments")
   public Set experiments() {
      return this.experiments;
   }
}
