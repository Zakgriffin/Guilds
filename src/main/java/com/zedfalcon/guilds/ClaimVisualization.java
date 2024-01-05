package com.zedfalcon.guilds;

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
import org.joml.Vector3f;

import java.util.*;

class ClaimVisualizationInfo {
    public final Claim claim;
    public final Set<BlockPos> previousShownBlockResistances;
    public int lowResistanceBound;
    public int highResistanceBound;
    public boolean changingLowerBound;

    public ClaimVisualizationInfo(Claim claim, Set<BlockPos> previousShownBlockResistances) {
        this.claim = claim;
        this.previousShownBlockResistances = previousShownBlockResistances;
        this.lowResistanceBound = 0;
        this.highResistanceBound = 0;
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

//    public void showClaimResistanceParticle(BlockPos blockPos, int resistance, ServerPlayerEntity player, World world) {
//        // map [0, 10] -> [0, 1]
//        int c = Color.HSBtoRGB(resistance / 10f, 1, 1);
//        DustParticleEffect particle = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(c)), 0.3f);
//
//        Vec3d v = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
//
//        double[] p = new double[]{0.45, 0, 0.45};
//        int[] flops = new int[]{1, -1};
//        for (int a : flops) {
//            for (int b : flops) {
//                double[] q = new double[]{p[0] * a, p[1], p[2] * b};
//                for (int i = 0; i < p.length; i++) {
//                    double x = q[i];
//                    double y = q[(i + 1) % 3];
//                    double z = q[(i + 2) % 3];
//                    ((ServerWorld) world).spawnParticles(player, particle, true,
//                            v.getX() + x, v.getY() + y, v.getZ() + z,
//                            10,
//                            x == 0 ? 0.2 : 0, y == 0 ? 0.2 : 0, z == 0 ? 0.2 : 0,
//                            0
//                    );
//                }
//            }
//        }
//    }

    public void showClaimResistance(BlockPos blockPos, int resistance, ServerPlayerEntity player) {
        sendBlockPacket(player, blockPos, blockStateFromResistance(resistance));
    }

    private BlockState blockStateFromResistance(int resistance) {
        return GLASS_BLOCK_STATES[resistance % GLASS_BLOCK_STATES.length];
    }

    private void sendBlockPacket(ServerPlayerEntity player, BlockPos blockPos, BlockState blockState) {
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, blockState));
    }

    public void showClaimOutlineTo(ServerPlayerEntity player, Claim claim) {
        List<BlockPos> outlineBlocks = claim.getOutlineBlocks();
        for (int i = 0; i < outlineBlocks.size(); i++) {
            BlockPos b1 = outlineBlocks.get(i);
            BlockPos b2 = outlineBlocks.get((i + 1) % outlineBlocks.size());
            double dist = Math.sqrt(b1.getSquaredDistance(b2));
            BlockPos vec = b2.subtract(b1);
            BlockPos step = new BlockPos(
                    (int) (vec.getX() / dist),
                    (int) (vec.getY() / dist),
                    (int) (vec.getZ() / dist)
            );
            for (int j = 0; j < dist; j++) {
                BlockPos l = b1.add(step.multiply(j));
                DustParticleEffect particle = new DustParticleEffect(new Vector3f(0xFFFF00), 1);

                ((ServerWorld) claim.getWorld()).spawnParticles(player, particle, true, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    public void updateLowResistanceBound(ServerPlayerEntity player, int newLowBound) {
        playerClaimVisualizationInfos.get(player).lowResistanceBound = newLowBound;
    }

    public void updateHighResistanceBound(ServerPlayerEntity player, int newHighBound) {
        playerClaimVisualizationInfos.get(player).highResistanceBound = newHighBound;
    }

    public void updateVisibleClaimResistancesToPlayer(ServerPlayerEntity player) {
        ClaimVisualizationInfo info = playerClaimVisualizationInfos.get(player);

        for (BlockPos previousShownBlock : info.previousShownBlockResistances) {
            sendBlockPacket(player, previousShownBlock, player.getWorld().getBlockState(previousShownBlock));
        }
        info.previousShownBlockResistances.clear();

        int squareRadius = 20;
        ClaimResistances claimResistances = info.claim.getClaimResistances();
        for (int y = -squareRadius; y <= squareRadius; y++) {
            for (int x = -squareRadius; x <= squareRadius; x++) {
                for (int z = -squareRadius; z <= squareRadius; z++) {
                    BlockPos pos = player.getBlockPos().add(x, y, z);
                    if (!claimResistances.inBounds(pos)) continue;

                    int resistance = claimResistances.getResistanceAt(pos);
                    if (resistance >= info.lowResistanceBound && resistance <= info.highResistanceBound) {
                        sendBlockPacket(player, pos, blockStateFromResistance(resistance));
                        info.previousShownBlockResistances.add(pos);
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
        int newLowBound = info.lowResistanceBound + delta;
        info.lowResistanceBound = newLowBound;
        player.sendMessage(Text.literal("Low Resistance Bound = " + newLowBound), true);
//        } else {
        int newHighBound = info.highResistanceBound + delta;
        info.highResistanceBound = newHighBound;
        player.sendMessage(Text.literal("High Resistance Bound = " + newHighBound), true);
//        }

        updateVisibleClaimResistancesToPlayer(player);
    }

    public void toggleResistanceBounds(ServerPlayerEntity player) {
        ClaimVisualizationInfo info = playerClaimVisualizationInfos.get(player);

        info.changingLowerBound = !info.changingLowerBound;
    }
}
