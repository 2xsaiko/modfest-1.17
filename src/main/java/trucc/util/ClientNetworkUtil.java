package trucc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.util.function.Function;

public final class ClientNetworkUtil {
    private ClientNetworkUtil() {
    }

    public static <T> PlayChannelHandler map(TypedPlayChannelHandler<T> ch, Function<PacketByteBuf, T> deserializer) {
        return (client, handler, buf, responseSender) -> {
            T message = deserializer.apply(buf);
            ch.receive(client, handler, message, responseSender);
        };
    }

    public interface TypedPlayChannelHandler<T> {
        void receive(MinecraftClient client, ClientPlayNetworkHandler handler, T packet, PacketSender responseSender);
    }
}
