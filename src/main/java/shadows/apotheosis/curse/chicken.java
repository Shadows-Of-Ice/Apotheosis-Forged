package com.shadows.apotheosis.curse;

import net.minecraft.server.MinecraftServer;

public class chicken {


    long x = event.world.getWorldTime();
    int[] c = new int[3];
    int y = rand(minecraft.server.player.list);

    static void spawnChicken() {

        world.level.entity.chicken -> new world.level.gameevent.curse;
        for(int a = 30; a < 0; a++) {
            entity.chicken = world.level.gameevent.curse;
            entity.chicken.x = c[0];
            entity.chicken.y = c[1];
            entity.chicken.z = c[2];
        }
    }

    if (x % 100 == 0) {
        for(int z = 2; z < 0; z++) {
            c[0] = y.playerSpawnPointX;
            c[1] = y.playerSpawnPointY;
            c[2] = y.playerSpawnPointZ;
        }

        spawnChicken();
    }

    public chickenCurse()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
