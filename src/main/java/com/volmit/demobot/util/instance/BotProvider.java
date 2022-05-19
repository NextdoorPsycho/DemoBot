package com.volmit.demobot.util.instance;

import com.volmit.demobot.Core;

import javax.security.auth.login.LoginException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class BotProvider implements IBotProvider {

    private final ExecutorService executorService;
    private final AtomicReference<String> token;
    private final AtomicReference<CompletableFuture<BotInstance>> future;

    public BotProvider() {
        token = new AtomicReference<>(realToken());
        future = new AtomicReference<>();
        executorService = Executors.newWorkStealingPool(8);
    }

    private String realToken() {
        return Core.get().botToken;
    }

    @Override
    public CompletableFuture<BotInstance> getFuture() {
        String rt = realToken();
        if (!token.get().equals(rt)) {
            future.set(null);
        }

        if (future.get() == null) {
            token.set(rt);
            future.set(CompletableFuture.supplyAsync(() -> {
                try {
                    return new BotInstance(token.get());
                } catch (LoginException | InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }, executorService));
        }

        return future.get();
    }
}