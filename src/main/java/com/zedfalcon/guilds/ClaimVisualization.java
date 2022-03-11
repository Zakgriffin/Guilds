package com.zedfalcon.guilds;

import com.zedfalcon.guilds.helpers.BlockPosTransforms;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ClaimVisualization {
    public static final ClaimVisualization INSTANCE = new ClaimVisualization();

    private final Map<ServerPlayerEntity, Claim> visibleClaims;

    private final static BlockState[] GLASS_BLOCK_STATES = Arrays.stream(new Block[] {
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
        this.visibleClaims = new HashMap<>();
    }

    public void addPlayerVisualizationForClaim(ServerPlayerEntity player, Claim claim) {
        player.playSound(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1f, 1f);
        visibleClaims.put(player, claim);
    }

    public void removePlayerVisualizationForClaim(ServerPlayerEntity player) {
        player.playSound(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 1f, 0.5f);
        visibleClaims.remove(player);
    }

    public void visualizeAllClaims() {
        for (var entry : visibleClaims.entrySet()) {
            ServerPlayerEntity player = entry.getKey();
            Claim claim = entry.getValue();
            showClaimOutlineTo(player, claim);
        }
    }

    public boolean isShowingForPlayer(ServerPlayerEntity player) {
        return visibleClaims.containsKey(player);
    }

    public void removeAllVisualizingClaim(Claim claim) {
        visibleClaims.values().removeAll(Collections.singleton(claim));
    }

    public void showClaimResistanceParticle(BlockPos blockPos, int resistance, ServerPlayerEntity player, World world) {
        // map [0, 10] -> [0, 1]
        int c = Color.HSBtoRGB(resistance / 10f, 1, 1);
        DustParticleEffect particle = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(c)), 0.3f);

        Vec3d v = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

        double[] p = new double[]{0.45, 0, 0.45};
        int[] flops = new int[]{1, -1};
        for (int a : flops) {
            for (int b : flops) {
                double[] q = new double[]{p[0] * a, p[1], p[2] * b};
                for (int i = 0; i < p.length; i++) {
                    double x = q[i];
                    double y = q[(i + 1) % 3];
                    double z = q[(i + 2) % 3];
                    ((ServerWorld) world).spawnParticles(player, particle, true,
                            v.getX() + x, v.getY() + y, v.getZ() + z,
                            10,
                            x == 0 ? 0.2 : 0, y == 0 ? 0.2 : 0, z == 0 ? 0.2 : 0,
                            0
                    );
                }
            }
        }
    }

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
                    vec.getX() / dist,
                    vec.getY() / dist,
                    vec.getZ() / dist
            );
            for (int j = 0; j < dist; j++) {
                BlockPos l = b1.add(step.multiply(j));
                DustParticleEffect particle = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(0xFFFF00)), 1);

                ((ServerWorld) claim.getWorld()).spawnParticles(player, particle, true, l.getX(), l.getY(), l.getZ(), 1, 0, 0, 0, 0);
            }
        }

        if (player.isHolding(Items.STICK) || player.isHolding(Items.BLAZE_ROD)) {
            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -4; z <= 4; z++) {
                        BlockPos pos = player.getBlockPos().add(x, y, z);
                        if (player.isHolding(Items.BLAZE_ROD) || !player.getWorld().getBlockState(pos).isAir()) {
                            showClaimResistance(pos, pos.getY() + pos.getX() + pos.getZ(), player);
                        }
                    }
                }
            }
        }
    }


    private final Map<ServerPlayerEntity, Claim> resistanceVisible = new HashMap<>();

    public void showClaimResistancesToPlayer(Claim claim, ServerPlayerEntity player) {
        boolean exists = resistanceVisible.containsKey(player);
        for(ChunkPos chunk : claim.getTouchingChunks()) {
            BlockPos lowestCorner = BlockPosTransforms.getMinPos(chunk);
            for(int y = 0; y < 255; y++) {
                for(int x = 0; x < 16; x++) {
                    for(int z = 0; z < 16; z++) {
                        BlockPos pos = lowestCorner.add(x,y,z);
                        if(exists) {
                            sendBlockPacket(player, pos, player.getWorld().getBlockState(pos));
                        } else {
                            int resistance = claim.getClaimResistances().getResistanceAt(pos);
                            sendBlockPacket(player, pos, blockStateFromResistance(resistance));
                        }
                    }
                }
            }
        }
        if(exists) {
            resistanceVisible.remove(player);
        } else {
            resistanceVisible.put(player, claim);
        }
    }
}
