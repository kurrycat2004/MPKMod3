package io.github.kurrycat.mpkmod.modules.main;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.IModuleEntry;
import io.github.kurrycat.mpkmod.api.module.ModuleEntrypoint;
import io.github.kurrycat.mpkmod.api.render.Render2D;

import java.awt.*;

public class MainModule implements ModuleEntrypoint {
    public static ILogger LOGGER;

    @Override
    public void onLoad(IModuleEntry entry, ILogger logger) {
        LOGGER = logger;
        LOGGER.info("Loading Main Module");
       /* List<String> testList = List.of("test1", "test2");
        LOGGER.info("TestList: {}", testList);*/
        Test test = new Test(1, 2);
        LOGGER.info("Test: " + test);
    }

    @Override
    public void onUnload() {

    }

    record Test(int a, int b) {}

    @Override
    public void onFrame() {
        Render2D.HANDLE.get().pushRect(10, 10, 30, 30, Color.RED.getRGB());
    }
}
