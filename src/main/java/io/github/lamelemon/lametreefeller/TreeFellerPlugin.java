package io.github.lamelemon.lametreefeller;


import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TreeFellerPlugin extends JavaPlugin implements Listener {

    public int maxLogsBroken = 500;  // change later to be from a config file

    // Lets us only check what is necessary (SELF is used for checking blocks under and above the broken block)
    private final TreeSet<BlockFace> faces = new TreeSet<>(Set.of(
        BlockFace.SELF,
        BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.EAST,
        BlockFace.SOUTH, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
        BlockFace.WEST // also allows me to at some point make it only check whats necessary
    ));

    @Override
    public void onEnable() {
        // Register as an event listener
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Treefeller enabled and registered as an event."); // debug for checking if plugin actually enables and class successfully registers as an event
    }

    @Override
    public void onDisable() {
        getLogger().info("Treefeller disabled."); // debug for checking if plugin actually disables
    }

    // Ran whenever a block is broken
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block start = event.getBlock(); // the block that was broken
        ItemStack heldAxe = player.getInventory().getItemInMainHand();

        // Skip if not holding axe and block isn't a log or player is sneaking
        if (isHoldingAxe(heldAxe.getType()) && isLog(start.getType()) && player.isSneaking()) {
            long startingTime = System.nanoTime();
            long timeout = 50_000_000;

            HashSet<Block> blocksToBreak = recursiveLogBreaker(
                    start,
                    new HashSet<>(Set.of(start)),
                    start.getType(),
                    startingTime,
                    timeout
            );

            getLogger().info("Found " + blocksToBreak.size() + " blocks to break and took " + ((System.nanoTime() - startingTime) / 1_000_000) + " milliseconds to do so.");

            int damageTaken = 0;

            for (Block block : blocksToBreak) {
                if (durabilityHandler(heldAxe, player)) damageTaken++;
                block.breakNaturally(heldAxe);
            }

            heldAxe.damage(damageTaken, player);
        }
    }



    private HashSet<Block> recursiveLogBreaker(
            Block block,
            HashSet<Block> blocksHandled,
            Material firstBlock,
            long startTime,
            long timeoutNanos
    ) {

        // Stop function if we have hit the max amount of logs broken
        if (blocksHandled.size() >= maxLogsBroken

            // Stop if log is not the right type or if they aren't leaves.
            || !(block.getType() == firstBlock || canBreak(block.getType()))

            // Time out the function so we don't get infinite recursion
            || System.nanoTime() - startTime > timeoutNanos
            ) {

            return blocksHandled;
        }

        blocksHandled.add(block);

        // Check all sides of the block broken
        for (BlockFace direction : faces) {
            if (blocksHandled.size() >= maxLogsBroken) return blocksHandled;

            Block foundBlock = block.getRelative(direction); // Get block in direction
            // Get blocks above and below the block in addition to itself
            HashSet<Block> foundBlocks = new HashSet<>(Set.of(foundBlock, foundBlock.getRelative(BlockFace.UP), foundBlock.getRelative(BlockFace.DOWN)));

            // Iterate through all 3 blocks, checking if they fit requirements.
            for (Block b : foundBlocks) {
                // check if treefeller is allowed to break the block and then attempt to add
                if (canBreak(b.getType()) && !blocksHandled.contains(b)) {
                    blocksHandled = recursiveLogBreaker(b, blocksHandled, firstBlock, startTime, timeoutNanos);
                }
            }
        }

        return  blocksHandled;
    }

    // account for unbreaking on player's axe
    private boolean durabilityHandler(ItemStack axe, Player player) {
        return player.getGameMode() != GameMode.CREATIVE && 1.0 / (axe.getEnchantmentLevel(Enchantment.UNBREAKING) + 1) > Math.random();
    }// this emulates the unbreaking calculation

    // Helper for checking if the block is a log/stem
    private boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private boolean canBreak(Material material) {
        return isLog(material) || Tag.LEAVES.isTagged(material) || Tag.WART_BLOCKS.isTagged(material) || material == Material.SHROOMLIGHT;
    }

    private boolean isHoldingAxe(Material type) {
        return Tag.ITEMS_AXES.isTagged(type);
    }
}
