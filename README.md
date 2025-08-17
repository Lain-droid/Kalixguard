# ApexGuard - Advanced Anti-Cheat System

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://adoptium.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21+-green.svg)](https://minecraft.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Gradle-orange.svg)](https://gradle.org/)

ApexGuard is a cutting-edge anti-cheat system designed for Minecraft servers that combines traditional detection methods with advanced machine learning techniques to catch sophisticated cheats while minimizing false positives.

## 🚀 Features

### Advanced Movement Detection
- **Fly/Glide Detection**: Sophisticated algorithms to detect various flying cheats
- **Jesus/LiquidWalk**: Detection of walking on water/lava
- **Phase/NoClip**: Collision detection and impossible movement validation
- **Timer/Blink**: Time manipulation and teleportation detection
- **Strafe/Omni-sprint**: Advanced movement pattern analysis
- **ElytraFly/RiptideFly**: Enhanced elytra and riptide cheat detection
- **Boat/Vehicle Speed**: Vehicle-based speed hack detection
- **Step/Spider**: Climbing and step cheat detection

### Combat Analysis
- **MultiAura/Hit-select**: Multi-target and selective targeting detection
- **Legit-hit Timing Model**: W-tap/S-tap pattern recognition
- **AimBot Detection**: Micro-jitter and jerk profiling
- **AutoBlock/Shield Abuse**: Automatic blocking detection
- **Velocity Manipulation**: Knockback accept/refuse modeling
- **Criticals Analysis**: Packet vs legitimate critical hit detection

### Inventory & Interaction Protection
- **NoSlow Detection**: Item use speed validation
- **Scaffold/Tower**: Building pattern analysis
- **XCarry/Inventory Desync**: Inventory manipulation detection
- **Ghost Hand/Use**: Invisible interaction detection
- **Offhand Bypasses**: 1.9+ offhand cheat detection

### Exploit & Crash Prevention
- **BookBan/NBT Size Caps**: Payload size validation
- **Map/Sign/JSON Validator**: Content validation and sanitization
- **PacketFlood/ChunkBan Protection**: DDoS and chunk bombing prevention
- **Crafting/Anvil Dupe Detection**: Duplication exploit prevention
- **Shulker/Book Dupe Protection**: Container manipulation detection
- **Unsafe Commands**: Command injection prevention

### Client Fingerprinting
- **Handshake/Brand Analysis**: Client identification and validation
- **Classpath Anomaly Detection**: Suspicious class loading detection
- **ViaVersion Protocol Support**: Multi-version compatibility
- **Bedrock (Geyser) Profile**: Cross-platform cheat detection

### Network & Protocol Analysis
- **KeepAlive RTT Modeling**: Round-trip time analysis with jitter detection
- **Packet Timeline Modeling**: Pacing, burst entropy, and periodicity analysis
- **Proxy-aware Correlation**: Velocity/Waterfall proxy detection
- **Address Spoofing Detection**: IP manipulation detection
- **Packet Shaping**: Adaptive throttling for flagged players

### Physics & Environment Validation
- **Per-block Friction/Traction**: Environment-aware movement validation
- **Fluid Dynamics**: Swim/sprint-swim and dolphin's grace analysis
- **Elytra/Riptide Kinematics**: Advanced movement physics
- **Boat Inertia & Collision**: Vehicle physics validation
- **3D Reach Ray-tracing**: Swept AABB with latency interpolation
- **Hitbox Expansion Budget**: Reach calculation with tolerance

### Machine Learning & Adaptive Detection
- **Per-player Baselines**: Individual player behavior modeling
- **EWMA + CUSUM Detection**: Statistical change detection
- **Robust Statistics**: Median/MAD thresholds with seasonal profiles
- **Unsupervised Clustering**: HDBSCAN-lite for anomaly scoring
- **Self-tuning**: Automatic threshold adjustment using ground truth
- **Cross-check Correlation**: Multi-signal risk assessment

### Performance & Infrastructure
- **Lock-free Algorithms**: Ring buffers and object pooling
- **Batch Evaluation**: Per-tick processing with adaptive sampling
- **Folia Support**: Multi-threaded server compatibility
- **Memory Management**: Efficient data structures and cleanup

### Replay & Analysis Tools
- **Binary Replay Format**: PCAP-like data with time indexing
- **Selective Export**: Configurable data export windows
- **Admin GUI**: In-game analytics and violation timeline
- **Offline Analyzer**: CLI tools for data analysis and tuning

### Self-Protection & Security
- **Jar Signature Verification**: Integrity checking and validation
- **Classpath Whitelisting**: Secure class loading
- **Checksum Beacons**: Critical class integrity monitoring
- **Debug Agent Detection**: JDWP and JVMTI monitoring
- **Config Integrity**: HMAC-based configuration validation

## 📋 Requirements

- **Java**: 21 or higher
- **Minecraft**: 1.8 - 1.21+ (Paper/Spigot)
- **ProtocolLib**: 5.1.0+ (recommended for full features)
- **Redis**: 6.0+ (optional, for multi-instance support)

## 🛠️ Installation

### 1. Download
Download the latest release from the [Releases](https://github.com/apexguard/apexguard/releases) page.

### 2. Dependencies
Place the following plugins in your `plugins` folder:
- `ApexGuard.jar`
- `ProtocolLib.jar` (recommended)

### 3. Configuration
The plugin will generate a comprehensive `config.yml` file on first run. Customize the settings according to your server's needs.

### 4. Restart
Restart your server to activate ApexGuard.

## ⚙️ Configuration

### Detection Profiles
ApexGuard comes with three pre-configured detection profiles:

- **safe_default**: Conservative detection with minimal false positives
- **aggressive_qa**: Balanced detection for quality assurance
- **tournament_strict**: Strict detection for competitive environments

### Physics Profiles
Environment-specific physics validation:

- **default**: Standard movement validation
- **ice**: Ice and slippery surface handling
- **water**: Aquatic movement validation

### Machine Learning
Configure ML parameters for optimal detection:

```yaml
ml:
  enabled: true
  learning-rate: 0.01
  batch-size: 100
  epochs: 1000
  auto-tune: true
```

### Redis Configuration
For multi-instance support:

```yaml
redis:
  enabled: true
  host: "localhost"
  port: 6379
  password: ""
  database: 0
```

## 🎮 Commands

### Main Commands
- `/apexguard` - Main administration command
- `/agstats` - View anti-cheat statistics
- `/agprofile` - Manage detection profiles
- `/agcheck` - Manually run checks on players

### Player Management
- `/agban` - Ban players for cheating
- `/agkick` - Kick suspicious players
- `/agwarn` - Issue warnings
- `/agslow` - Slow down player movement

### Analysis & Tools
- `/agreplay` - View player replay data
- `/aglogs` - Access anti-cheat logs
- `/agml` - Machine learning management
- `/agphysics` - Physics engine control

### System Management
- `/agconfig` - Configuration management
- `/agbackup` - Data backup and restore
- `/agperformance` - Performance monitoring
- `/agredis` - Redis connection management

## 🔧 Advanced Configuration

### Custom Detection Rules
Create custom detection rules using the rule graph system:

```yaml
rule-graph:
  speed_spike:
    conditions:
      - "speed > threshold"
      - "timer_pacing < min_pacing"
      - "jitter < max_jitter"
    action: "flag"
    severity: "high"
```

### Performance Tuning
Optimize performance for your server:

```yaml
performance:
  max-threads: 8
  batch-size: 50
  queue-size: 1000
  memory-limit: 512
```

### Check-Specific Settings
Fine-tune individual checks:

```yaml
checks:
  Speed:
    enabled: true
    threshold: 4.0
    max-violations: 20
    cooldown: 1000
```

## 📊 Monitoring & Analytics

### Real-time Dashboard
Access comprehensive analytics through the in-game admin GUI:
- Player violation timeline
- Check performance metrics
- Network analysis graphs
- ML model statistics

### Logging & Export
- JSON-formatted logs for external analysis
- Configurable log rotation and retention
- Export tools for data analysis
- Integration with external monitoring systems

### Performance Metrics
- TPS impact monitoring
- Memory usage tracking
- Check execution times
- False positive/negative rates

## 🔒 Security Features

### Anti-Tamper Protection
- Jar signature verification
- Class integrity monitoring
- Configuration tampering detection
- Debug agent prevention

### Self-Protection
- Reflection attack prevention
- Stack trace monitoring
- Attach mechanism detection
- Instrumentation monitoring

## 🌐 Multi-Instance Support

### Redis Integration
- Shared player data across instances
- Centralized configuration management
- Cross-instance violation correlation
- Load balancing and failover support

### Proxy Awareness
- Velocity/Waterfall integration
- Session correlation across proxies
- IP spoofing detection
- Multi-instance risk assessment

## 🧪 Testing & Development

### Simulation Tools
- Built-in cheat simulation clients
- Property-based testing framework
- Regression testing suite
- Performance benchmarking tools

### CI/CD Integration
- Automated testing matrix
- Version compatibility testing
- Performance regression detection
- Automated deployment support

## 📈 Performance Optimization

### Lock-free Algorithms
- Ring buffer implementations
- Object pooling for GC reduction
- Batch processing optimization
- Adaptive sampling under load

### Memory Management
- Efficient data structures
- Automatic cleanup and garbage collection
- Memory usage monitoring
- Configurable memory limits

## 🔧 Troubleshooting

### Common Issues

#### High False Positive Rate
1. Adjust detection thresholds in configuration
2. Use more conservative detection profiles
3. Review and tune ML parameters
4. Check for legitimate edge cases

#### Performance Impact
1. Reduce batch sizes and queue limits
2. Disable non-essential checks
3. Optimize ML model parameters
4. Monitor TPS and memory usage

#### Detection Gaps
1. Review check configurations
2. Analyze violation patterns
3. Update ML models with new data
4. Consider custom detection rules

### Debug Mode
Enable debug mode for detailed logging:

```yaml
general:
  debug: true
  log-level: "DEBUG"
```

### Support
For additional support:
- [GitHub Issues](https://github.com/apexguard/apexguard/issues)
- [Documentation Wiki](https://github.com/apexguard/apexguard/wiki)
- [Discord Community](https://discord.gg/apexguard)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on how to submit pull requests, report issues, and contribute to the project.

### Development Setup
1. Clone the repository
2. Install Java 21+
3. Run `./gradlew build`
4. Import into your IDE

### Testing
- Run unit tests: `./gradlew test`
- Run integration tests: `./gradlew integrationTest`
- Build plugin: `./gradlew jar`

## 🙏 Acknowledgments

- **ProtocolLib Team** - For excellent packet handling library
- **Paper Team** - For the amazing server software
- **Minecraft Community** - For feedback and testing
- **Open Source Contributors** - For various libraries and tools

## 📞 Contact

- **Project**: [GitHub](https://github.com/apexguard/apexguard)
- **Issues**: [GitHub Issues](https://github.com/apexguard/apexguard/issues)
- **Discussions**: [GitHub Discussions](https://github.com/apexguard/apexguard/discussions)
- **Wiki**: [Documentation](https://github.com/apexguard/apexguard/wiki)

---

**ApexGuard** - Protecting Minecraft servers with intelligence and precision.

*Built with ❤️ for the Minecraft community*