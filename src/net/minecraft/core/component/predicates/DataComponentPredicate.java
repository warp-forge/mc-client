package net.minecraft.core.component.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentPredicate {
   Codec CODEC = Codec.dispatchedMap(DataComponentPredicate.Type.CODEC, Type::codec);
   StreamCodec SINGLE_STREAM_CODEC = DataComponentPredicate.Type.STREAM_CODEC.dispatch(Single::type, Type::singleStreamCodec);
   StreamCodec STREAM_CODEC = SINGLE_STREAM_CODEC.apply(ByteBufCodecs.list(64)).map((singles) -> (Map)singles.stream().collect(Collectors.toMap(Single::type, Single::predicate)), (map) -> map.entrySet().stream().map(Single::fromEntry).toList());

   static MapCodec singleCodec(final String name) {
      return DataComponentPredicate.Type.CODEC.dispatchMap(name, Single::type, Type::wrappedCodec);
   }

   boolean matches(DataComponentGetter components);

   public interface Type {
      Codec CODEC = Codec.either(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec(), BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()).xmap(Type::copyOrCreateType, Type::unpackType);
      StreamCodec STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.registry(Registries.DATA_COMPONENT_PREDICATE_TYPE), ByteBufCodecs.registry(Registries.DATA_COMPONENT_TYPE)).map(Type::copyOrCreateType, Type::unpackType);

      private static Either unpackType(final Type type) {
         Either var10000;
         if (type instanceof AnyValueType anyCheck) {
            var10000 = Either.right(anyCheck.componentType());
         } else {
            var10000 = Either.left(type);
         }

         return var10000;
      }

      private static Type copyOrCreateType(final Either concreteTypeOrComponent) {
         return (Type)concreteTypeOrComponent.map((concrete) -> concrete, AnyValueType::create);
      }

      Codec codec();

      MapCodec wrappedCodec();

      StreamCodec singleStreamCodec();
   }

   public abstract static class TypeBase implements Type {
      private final Codec codec;
      private final MapCodec wrappedCodec;
      private final StreamCodec singleStreamCodec;

      public TypeBase(final Codec codec) {
         this.codec = codec;
         this.wrappedCodec = DataComponentPredicate.Single.wrapCodec(this, codec);
         this.singleStreamCodec = ByteBufCodecs.fromCodecWithRegistries(codec).map((v) -> new Single(this, v), Single::predicate);
      }

      public Codec codec() {
         return this.codec;
      }

      public MapCodec wrappedCodec() {
         return this.wrappedCodec;
      }

      public StreamCodec singleStreamCodec() {
         return this.singleStreamCodec;
      }
   }

   public static final class ConcreteType extends TypeBase {
      public ConcreteType(final Codec codec) {
         super(codec);
      }
   }

   public static final class AnyValueType extends TypeBase {
      private final AnyValue predicate;

      public AnyValueType(final AnyValue predicate) {
         super(MapCodec.unitCodec(predicate));
         this.predicate = predicate;
      }

      public AnyValue predicate() {
         return this.predicate;
      }

      public DataComponentType componentType() {
         return this.predicate.type();
      }

      public static AnyValueType create(final DataComponentType componentType) {
         return new AnyValueType(new AnyValue(componentType));
      }
   }

   public static record Single(Type type, DataComponentPredicate predicate) {
      private static MapCodec wrapCodec(final Type type, final Codec codec) {
         return RecordCodecBuilder.mapCodec((i) -> i.group(codec.fieldOf("value").forGetter(Single::predicate)).apply(i, (predicate) -> new Single(type, predicate)));
      }

      private static Single fromEntry(final Map.Entry e) {
         return new Single((Type)e.getKey(), (DataComponentPredicate)e.getValue());
      }
   }
}
