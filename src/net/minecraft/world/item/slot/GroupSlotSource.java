package net.minecraft.world.item.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;

public class GroupSlotSource extends CompositeSlotSource {
   public static final MapCodec MAP_CODEC = createCodec(GroupSlotSource::new);
   public static final Codec INLINE_CODEC = createInlineCodec(GroupSlotSource::new);

   private GroupSlotSource(final List terms) {
      super(terms);
   }

   public MapCodec codec() {
      return MAP_CODEC;
   }
}
