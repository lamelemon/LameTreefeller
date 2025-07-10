package io.github.lamelemon.lametreefeller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

public class TreeFellerAPI {
    private static HashMap<String, HashSet<Material>> treeStructure;

    // Lets us only check what is necessary (SELF is used for checking blocks under and above the broken block)
    private static final HashSet<BlockFace> faces = new HashSet<>(
            Set.of(
                BlockFace.SELF,
                BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
                BlockFace.EAST,
                BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
                BlockFace.WEST
            )
    );

    // Loads json file for storing what logs and leaves generate on the same trees
    public static void loadTreeStructureFromJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = TreeFellerAPI.class.getClassLoader().getResourceAsStream("tree_structure.json");

            treeStructure = objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (IOException e) {
            TreeFellerPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to load JSON", e);
        }
    }

    public static HashSet<Material> getTreeStructure(String logName) {
        return treeStructure.get(logName);
    }

    public static HashSet<BlockFace> getFaces() {
        return faces;
    }

    public static HashSet<BlockFace> getFaces(HashSet<BlockFace> ignoredFaces) {
        HashSet<BlockFace> returnFaces = faces;

        for (BlockFace blockFace : ignoredFaces) {
            returnFaces.remove(blockFace);
        }

        return returnFaces;
    }

    public static HashSet<BlockFace> getFaces(BlockFace ignoredFace) {
        HashSet<BlockFace> returnFaces = faces;
        returnFaces.remove(ignoredFace);
        return returnFaces;
    }
}
