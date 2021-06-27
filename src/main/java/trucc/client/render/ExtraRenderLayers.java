package trucc.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class ExtraRenderLayers extends RenderLayer {
    private static final Function<Identifier, RenderLayer> ENTITY_CUTOUT_TRIS;

    private ExtraRenderLayers(String name, VertexFormat vertexFormat, DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new IllegalStateException("don't instantiate this");
    }

    public static RenderLayer getEntityCutoutTris(Identifier id) {
        return ENTITY_CUTOUT_TRIS.apply(id);
    }

    static {
        ENTITY_CUTOUT_TRIS = Util.memoize((texture) -> {
            RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .shader(ENTITY_CUTOUT_SHADER)
                .texture(new RenderPhase.Texture(texture, false, false))
                .transparency(NO_TRANSPARENCY)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(true);

            return RenderLayer.of("entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.TRIANGLES, 256, true, false, multiPhaseParameters);
        });
    }
}
