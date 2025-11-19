package com.example.examplemod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final String MOD_NAME = "Example Mod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("has been loaded!");
    }
}
