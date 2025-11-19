package com.example.examplemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static final String MOD_NAME = "Example Mod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public ExampleMod(FMLJavaModLoadingContext context) {
        LOGGER.info("has been loaded!");
    }
}