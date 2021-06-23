package trucc.client.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import trucc.entity.CableTravelerEntity;

public class CableTravelerRenderer extends EntityRenderer<CableTravelerEntity> {
    public CableTravelerRenderer(Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CableTravelerEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // no-op, this entity is invisible
    }

    @Override
    public Identifier getTexture(CableTravelerEntity entity) {
        return new Identifier("missingno");
    }
}
