package com.foliaCustomPrefix.data;

public record PrefixData(String prefix, long lastChange) {

    public boolean hasPrefix() {
        return prefix != null && !prefix.isEmpty();
    }
}
