package trucc.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import trucc.Trucc;
import trucc.client.render.WheelRenderer;

public class MotorWheelBlockEntity extends BlockEntity {
    private float speed;

    public MotorWheelBlockEntity(BlockPos pos, BlockState state) {
        super(Trucc.getInstance().blockEntityTypes.motorWheel, pos, state);
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.markDirty();
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        World world = this.getWorld();

        if (world != null && world.isClient() && world instanceof ClientWorld cw) {
            WheelRenderer.get(cw).remove(this.getPos());
        }
    }

    @Override
    public void cancelRemoval() {
        World world = this.getWorld();

        if (world != null && world.isClient() && world instanceof ClientWorld cw) {
            WheelRenderer.get(cw).add(this.getPos());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.speed = nbt.getFloat("speed");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putFloat("speed", this.speed);
        return super.writeNbt(nbt);
    }
}
