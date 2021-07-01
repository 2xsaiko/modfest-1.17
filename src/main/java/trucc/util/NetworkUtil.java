package trucc.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;

import java.util.function.BiConsumer;
import java.util.function.Function;

import io.netty.buffer.Unpooled;

public final class NetworkUtil {
    private NetworkUtil() {
    }

    public static <T> PlayChannelHandler map(TypedPlayChannelHandler<T> ch, Function<PacketByteBuf, T> deserializer) {
        return (server, player, handler, buf, responseSender) -> {
            T message = deserializer.apply(buf);
            ch.receive(server, player, handler, message, responseSender);
        };
    }

    public static <T> void send(T packet, ServerPlayerEntity receiver, Identifier id, BiConsumer<T, PacketByteBuf> serializer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        serializer.accept(packet, buf);
        ServerPlayNetworking.send(receiver, id, buf);
    }

    public interface TypedPlayChannelHandler<T> {
        void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, T packet, PacketSender responseSender);
    }
}
