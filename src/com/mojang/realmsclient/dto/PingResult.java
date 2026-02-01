package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record PingResult(List pingResults, List realmIds) implements ReflectionBasedSerialization {
   @SerializedName("pingResults")
   public List pingResults() {
      return this.pingResults;
   }

   @SerializedName("worldIds")
   public List realmIds() {
      return this.realmIds;
   }
}
