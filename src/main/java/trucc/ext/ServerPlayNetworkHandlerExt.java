package trucc.ext;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;

public interface ServerPlayNetworkHandlerExt {
    int requestTeleportId(Vec3d pos);

    static ServerPlayNetworkHandlerExt from(ServerPlayNetworkHandler self) {
        return (ServerPlayNetworkHandlerExt) (Object) self;
    }
}
