package com.zedfalcon.guilds;

import com.zedfalcon.guilds.claim.Claim;
import com.zedfalcon.guilds.claim.BlastShieldReaches;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.List;

import static net.minecraft.world.Heightmap.Type.WORLD_SURFACE;

class ClaimVisualizationInfo {
    public final Claim claim;
    public final Set<BlockPos> previousShownBlastShieldReaches;
    public int lowBlastShieldReachBound;
    public int highBlastShieldReachBound;
    public boolean changingLowerBound;

    public ClaimVisualizationInfo(Claim claim, Set<BlockPos> previousShownBlastShieldReaches) {
        this.claim = claim;
        this.previousShownBlastShieldReaches = previousShownBlastShieldReaches;
        this.lowBlastShieldReachBound = 0;
        this.highBlastShieldReachBound = 0;
        this.changingLowerBound = false;
    }
}

public class ClaimVisualization {
    public static final ClaimVisualization INSTANCE = new ClaimVisualization();

    private final Map<ServerPlayerEntity, ClaimVisualizationInfo> playerClaimVisualizationInfos;

    private final static BlockState[] GLASS_BLOCK_STATES = Arrays.stream(new Block[]{
            Blocks.RED_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS
    }).map(Block::getDefaultState).toArray(BlockState[]::new);

    public ClaimVisualization() {
        this.playerClaimVisualizationInfos = new HashMap<>();
    }

    public void addPlayerVisualizationForClaim(ServerPlayerEntity player, Claim claim) {
        player.playSound(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1f, 1f);
        ClaimVisualizationInfo info = new ClaimVisualizationInfo(claim, new TreeSet<>());
        playerClaimVisualizationInfos.put(player, info);
    }

    public void removePlayerVisualizationForClaim(ServerPlayerEntity player) {
        player.playSound(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1f, 0.5f);
        playerClaimVisualizationInfos.remove(player);
    }

    public void visualizeAllClaims() {
        for (Map.Entry<ServerPlayerEntity, ClaimVisualizationInfo> entry : playerClaimVisualizationInfos.entrySet()) {
            ServerPlayerEntity player = entry.getKey();
            ClaimVisualizationInfo info = entry.getValue();
            showClaimOutlineTo(player, info.claim);
        }
    }

    public boolean isShowingForPlayer(ServerPlayerEntity player) {
        return playerClaimVisualizationInfos.containsKey(player);
    }

    public void removeAllVisualizingClaim(Claim claim) {
        playerClaimVisualizationInfos.values().removeIf(info -> info.claim == claim);
    }

    private BlockState blockStateFromBlastShieldReach(int blastShieldReach) {
        return GLASS_BLOCK_STATES[blastShieldReach % GLASS_BLOCK_STATES.length];
    }

    public static void bonk(ServerPlayerEntity player, BlockPos blockPos, int blastShieldReach) {
        sendBlockPacket(player, blockPos, GLASS_BLOCK_STATES[blastShieldReach % GLASS_BLOCK_STATES.length]);
    }

    public static void bonkClear(ServerPlayerEntity player, BlockPos blockPos) {
        sendBlockPacket(player, blockPos, player.getWorld().getBlockState(blockPos));
    }

    private static void sendBlockPacket(ServerPlayerEntity player, BlockPos blockPos, BlockState blockState) {
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, blockState));
    }

    public void showClaimOutlineTo(ServerPlayerEntity player, Claim claim) {
        List<BlockPos> outlineBlocks = claim.getOutlineBlocks();
        for (int i = 0; i < outlineBlocks.size(); i++) {
            Vec3d p1 = outlineBlocks.get(i).toCenterPos();
            Vec3d p2 = outlineBlocks.get((i + 1) % outlineBlocks.size()).toCenterPos();
            double dist = p2.distanceTo(p1);
            Vec3d step = p2.subtract(p1).normalize();
            for (int j = 0; j < dist; j += 1) {
                Vec3d l = p1.add(step.multiply(j));
                DustParticleEffect particle = new DustParticleEffect(Vec3d.unpackRgb(0xFFFF00).toVector3f(), 1);

                ServerWorld world = claim.getWorld();
                int topY = world.getTopY(WORLD_SURFACE, (int) l.x, (int) l.z);
                world.spawnParticles(player, particle, true, l.x, Math.max(l.y, topY), l.z, 1, 0, 0, 0, 0);
            }
        }
    }
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 214.5)
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 213.5)
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 212.5)
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 211.5)
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 210.5)
//[01:15:05] [Server thread/INFO] (Minecraft) [STDOUT]: WHAT: (194.5, 90.5, 209.5)

    public void updateLowBlastShieldReachBound(ServerPlayerEntity player, int newLowBound) {
        playerClaimVisualizationInfos.get(player).lowBlastShieldReachBound = newLowBound;
    }

    public void updateHighBlastShieldReachBound(ServerPlayerEntity player, int newHighBound) {
        playerClaimVisualizationInfos.get(player).highBlastShieldReachBound = newHighBound;
    }

    public void updateVisibleClaimBlastShieldReachesToPlayer(ServerPlayerEntity player) {
        ClaimVisualizationInfo info = playerClaimVisualizationInfos.get(player);

        for (BlockPos previousShownBlock : info.previousShownBlastShieldReaches) {
            sendBlockPacket(player, previousShownBlock, player.getWorld().getBlockState(previousShownBlock));
        }
        info.previousShownBlastShieldReaches.clear();

        int squareRadius = 20;
        BlastShieldReaches blastShieldReaches = info.claim.getBlastShieldReaches();
        for (int y = -squareRadius; y <= squareRadius; y++) {
            for (int x = -squareRadius; x <= squareRadius; x++) {
                for (int z = -squareRadius; z <= squareRadius; z++) {
                    BlockPos pos = player.getBlockPos().add(x, y, z);
                    if (blastShieldReaches.outOfBounds(pos)) continue;

                    int blastShieldReach = blastShieldReaches.getBlastShieldReachAt(pos);
                    if (blastShieldReach >= info.lowBlastShieldReachBound && blastShieldReach <= info.highBlastShieldReachBound) {
                        sendBlockPacket(player, pos, blockStateFromBlastShieldReach(blastShieldReach));
                        info.previousShownBlastShieldReaches.add(pos);
                    }
                }
            }
        }
    }


    public void updatePlayerSelectedSlot(ServerPlayerEntity player, int newSlot) {
        ClaimVisualizationInfo info = playerClaimVisualizationInfos.get(player);
        int delta = newSlot - player.getInventory().selectedSlot;
        int hotBarSize = PlayerInventory.getHotbarSize();
        if (delta > hotBarSize / 2) {
            delta -= hotBarSize;
        } else if (delta < -hotBarSize / 2) {
            delta += hotBarSize;
        }

//        if(info.changingLowerBound) {
        int newLowBound = info.lowBlastShieldReachBound + delta;
        info.lowBlastShieldReachBound = newLowBound;
        player.sendMessage(Text.literal("Low Blast Shield Reach Bound = " + newLowBound), true);
//        } else {
        int newHighBound = info.highBlastShieldReachBound + delta;
        info.highBlastShieldReachBound = newHighBound;
        player.sendMessage(Text.literal("High Blast Shield Reach Bound = " + newHighBound), true);
//        }

        updateVisibleClaimBlastShieldReachesToPlayer(player);
    }

    public void toggleAdjustingBounds(ServerPlayerEntity player) {
        ClaimVisualizationInfo info = playerClaimVisualizationInfos.get(player);

        info.changingLowerBound = !info.changingLowerBound;
    }
}
