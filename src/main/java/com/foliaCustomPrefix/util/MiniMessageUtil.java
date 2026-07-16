package com.foliaCustomPrefix.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

public final class MiniMessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build();
    private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\p{Cntrl}\\n\\r]");
    private static final Pattern LEGACY_CODES = Pattern.compile("(?i)[&§](#[0-9a-f]{6}|x([&§][0-9a-f]){6}|[0-9a-fk-or])");

    private MiniMessageUtil() {
    }

    public static Component parse(String input) {
        if (LEGACY_CODES.matcher(input).find()) {
            return LEGACY_AMPERSAND.deserialize(input.replace('§', '&'));
        }
        return MINI_MESSAGE.deserialize(input);
    }

    public static String toLegacy(String input) {
        return LEGACY_SECTION.serialize(parse(input));
    }

    public static boolean isValid(String input) {
        try {
            parse(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean containsIllegalCharacters(String input) {
        return ILLEGAL_CHARACTERS.matcher(input).find();
    }

    public static String plainText(String input) {
        return PLAIN.serialize(parse(input));
    }
}
