package io.titix.kiwi.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class UniqueUUIDGenerator {

    private final Set<String> generatedIds;

    public UniqueUUIDGenerator() {
        generatedIds = new HashSet<>();
    }

    public String next() {
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (generatedIds.contains(uuid));
        generatedIds.add(uuid);
        return uuid;
    }
}
