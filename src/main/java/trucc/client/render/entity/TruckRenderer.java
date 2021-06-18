package trucc.client.render.entity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

import trucc.entity.TruckEntity;

import static trucc.Trucc.MOD_ID;

public class TruckRenderer extends EntityRenderer<TruckEntity> {
    private static final Identifier TEXTURE = new Identifier(MOD_ID, "textures/entity/truck.png");

    public TruckRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(TruckEntity entity) {
        return TEXTURE;
    }
}
