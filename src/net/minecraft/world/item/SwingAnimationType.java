package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum SwingAnimationType implements StringRepresentable {
   NONE(0, "none"),
   WHACK(1, "whack"),
   STAB(2, "stab");

   private static final IntFunction BY_ID = ByIdMap.continuous(SwingAnimationType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
   public static final Codec CODEC = StringRepresentable.fromEnum(SwingAnimationType::values);
   public static final StreamCodec STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SwingAnimationType::getId);
   private final int id;
   private final String name;

   private SwingAnimationType(final int id, final String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public String getSerializedName() {
      return this.name;
   }

   // $FF: synthetic method
   private static SwingAnimationType[] $values() {
      return new SwingAnimationType[]{NONE, WHACK, STAB};
   }
}
