package com.apexguard.logging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class Console {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private Console() {}

    public static void info(CommandSender sender, String message) {
        send(sender, message);
    }

    public static void success(CommandSender sender, String message) {
        send(sender, message);
    }

    public static void warn(CommandSender sender, String message) {
        send(sender, message);
    }

    public static void error(CommandSender sender, String message) {
        send(sender, message);
    }

    private static void send(CommandSender sender, String message) {
        Component component = SERIALIZER.deserialize(message);
        sender.sendMessage(component);
    }
}