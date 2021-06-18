package trucc;

import java.util.Objects;

import trucc.init.Blocks;
import trucc.init.Items;

public class Trucc {
    public static final String MOD_ID = "trucc";

    private static Trucc INSTANCE;
    public final Blocks blocks = new Blocks();
    public final Items items = new Items(this.blocks);

    public static void initialize() {
        Trucc instance = new Trucc();

        instance.blocks.register();
        instance.items.register();

        INSTANCE = instance;
    }

    public static Trucc getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }
}
