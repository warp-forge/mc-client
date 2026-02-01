package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record PreferredRegionsDto(List regionData) implements ReflectionBasedSerialization {
   public static PreferredRegionsDto empty() {
      return new PreferredRegionsDto(List.of());
   }

   @SerializedName("regionDataList")
   public List regionData() {
      return this.regionData;
   }
}
