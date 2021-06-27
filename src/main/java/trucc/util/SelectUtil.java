package trucc.util;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import net.dblsaiko.qcommon.croco.Mat4;
import net.dblsaiko.qcommon.croco.Vec3;

public class SelectUtil {
    private Mat4 guiMvp;
    private Mat4 worldMvp;

    public DeprojectResult cursorToWorld(int x, int y) {
        if (this.guiMvp == null || this.worldMvp == null) {
            return null;
        }

        Vec3 start = new Vec3(x, y, 0);
        Vec3 end = new Vec3(x, y, 1);
        Vec3 startClipSpace = this.guiMvp.mul(start);
        Vec3 endClipSpace = this.guiMvp.mul(start);
        Vec3 startWorld = this.worldMvp.invert().mul(startClipSpace);
        Vec3 endWorld = this.worldMvp.invert().mul(endClipSpace);
        Vec3 dir = endWorld.sub(startWorld).getNormalized();
        return new DeprojectResult(startWorld.toVec3d(), dir.toVec3d());
    }

    public Vec2f worldToCursor(Vec3d pos) {
        if (this.guiMvp == null || this.worldMvp == null) {
            return null;
        }

        Vec3 posClipSpace = this.worldMvp.mul(Vec3.from(pos));
        Vec3 posGui = this.guiMvp.invert().mul(posClipSpace);
        return new Vec2f(posGui.x, posGui.y);
    }

    public void saveGuiMatrices(Matrix4f projection, Matrix4f modelview) {
        Mat4 p = Mat4.fromMatrix4f(projection);
        Mat4 mv = Mat4.fromMatrix4f(modelview);
        this.guiMvp = p.mul(mv);
    }

    public void saveWorldMatrices(Matrix4f projection, Matrix4f modelview) {
        Mat4 p = Mat4.fromMatrix4f(projection);
        Mat4 mv = Mat4.fromMatrix4f(modelview);
        this.worldMvp = p.mul(mv);
    }

    public record DeprojectResult(Vec3d pos, Vec3d dir) {
    }
}
