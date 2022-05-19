package com.volmit.demobot.util.instance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface IBotProvider {
    CompletableFuture<BotInstance> getFuture();

    default BotInstance get() {
        try {
            return getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
