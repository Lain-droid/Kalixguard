# ApexGuard

A high-performance, adaptive, network-aware Anti-Cheat for Paper (MC 1.8–1.21.x), built with Java 21.

- Modular checks: movement, combat, packet, exploit, inventory, crash/dupe guard
- Async core, zero main-thread heavy work
- ML/statistical profiling with adaptive thresholds
- Packet replay, JSON logging, admin tools
- ProtocolLib adapter for packet I/O; degrades gracefully when absent

## Build

```bash
./gradlew shadowJar
```

The output plugin jar will be at `build/libs/ApexGuard.jar`.

## Install

- Drop the jar into `plugins/`
- (Optional) Install ProtocolLib for packet-level detection and replay
- Start server; configure `plugins/ApexGuard/config.yml`

## Commands

- `/apexguard profile <safe_default|aggressive_qa|tournament_strict>`
- `/apexguard verbose <player>`
- `/apexguard replay export <player> <seconds>`

## Notes

- No telemetry or auto-update. All heavy computation runs off the main thread.
- Ban is off by default; uses flag → warn → slow → kick. Enable ban in `config.yml` if desired.