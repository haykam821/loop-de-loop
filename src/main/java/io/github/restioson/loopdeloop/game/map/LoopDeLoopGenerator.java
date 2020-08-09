package io.github.restioson.loopdeloop.game.map;

import io.github.restioson.loopdeloop.game.LoopDeLoopConfig;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.system.MathUtil;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class LoopDeLoopGenerator {
    public final LoopDeLoopConfig config;

    public LoopDeLoopGenerator(LoopDeLoopConfig config) {
        this.config = config;
    }

    public CompletableFuture<LoopDeLoopMap> create() {
        return CompletableFuture.supplyAsync(this::build, Util.getServerWorkerExecutor());
    }

    private LoopDeLoopMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        LoopDeLoopMap map = new LoopDeLoopMap(template);

        this.spawnPlatform(template, map);

        BlockPos.Mutable circlePos = new BlockPos.Mutable();
        circlePos.set(0, 75, 20);
        Random random = new Random();

        for (int i = 0; i < this.config.loops; i++) {
            this.addCircle(template, 5, circlePos.toImmutable(), map);

            // New circle
            double z_scale = ((1.5 * this.config.loops) - i) / ((double) this.config.loops * 1.5);
            int z_move = MathHelper.nextInt(random, (int) Math.ceil((32 * z_scale)), (int) Math.ceil((64 * z_scale)));
            int y = MathHelper.nextInt(random, 75 - 16, 75 + 16);
            int x_move = MathHelper.nextInt(random, -16, 16);
            circlePos.move(Direction.SOUTH, z_move);
            circlePos.move(Direction.EAST, x_move);
            circlePos.setY(y);
        }

        map.setSpawn(new BlockPos(0, 65, 0));

        return map;
    }

    private void spawnPlatform(MapTemplate template, LoopDeLoopMap map) {
        BlockPos min = new BlockPos(-5, 64, -5);
        BlockPos max = new BlockPos(5, 64, 5);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            template.setBlockState(pos, Blocks.RED_TERRACOTTA.getDefaultState());
        }
    }

    private void addCircle(MapTemplate template, int radius, BlockPos centre, LoopDeLoopMap map) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        map.addHoop(new LoopDeLoopHoop(centre, radius));

        int radius2 = radius * radius;
        int outlineRadius2 = (radius - 1) * (radius - 1);

        BlockState outline = Blocks.BLUE_TERRACOTTA.getDefaultState();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                int distance2 = x * x + y * y;
                if (distance2 >= radius2) {
                    continue;
                }

                if (distance2 >= outlineRadius2) {
                    mutablePos.set(centre.getX() + x, centre.getY() + y, centre.getZ());
                    template.setBlockState(mutablePos, outline);
                }
            }
        }
    }
}
