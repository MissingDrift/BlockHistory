package me.missingdrift.blockhistory.blockstate;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlockStateCodecs {
    private static Map<Material, BlockStateCodec> codecs = new EnumMap<>(Material.class);

    public static void registerCodec(BlockStateCodec codec) {
        Material[] materials = codec.getApplicableMaterials();
        for (Material material : materials) {
            if (codecs.containsKey(material)) {
                throw new IllegalArgumentException("BlockStateCodec for " + material + " already registered!");
            }
            codecs.put(material, codec);
        }
    }

    static {
        registerCodec(new BlockStateCodecSign());
        registerCodec(new BlockStateCodecSkull());
        registerCodec(new BlockStateCodecBanner());
        registerCodec(new BlockStateCodecSpawner());
        registerCodec(new BlockStateCodecLectern());
        registerCodec(new BlockStateCodecShulkerBox());
    }

    public static boolean hasCodec(Material material) {
        return codecs.containsKey(material);
    }

    public static YamlConfiguration serialize(BlockState state) {
        BlockStateCodec codec = codecs.get(state.getType());
        if (codec != null) {
            YamlConfiguration serialized = codec.serialize(state);
            if (serialized != null && !serialized.getKeys(false).isEmpty()) {
                return serialized;
            }
        }
        return null;
    }

    public static void deserialize(BlockState block, YamlConfiguration state) {
        BlockStateCodec codec = codecs.get(block.getType());
        if (codec != null) {
            codec.deserialize(block, state);
        }
    }

    public static String toString(Material material, YamlConfiguration state, YamlConfiguration oldState) {
        BlockStateCodec codec = codecs.get(material);
        if (codec != null) {
            return codec.toString(state, oldState);
        }
        return null;
    }
}
