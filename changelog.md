# 13.1.0
- Implement enchantment level specification for item selectors
    - See [examples here](https://github.com/dexman545/Fabric-Autoswitch/blob/master/fabric/src/gametest/resources/configs/enchantmentLevelTest.conf)
- Add debug huds to show enchantment ids and potion contents of the currently held item. 
  - Access via `F3+F6`
  - Named `autoswitch:enchantments` and `autoswitch:item_components`

# 13.0.0
- Update for 1.21.9/25w37a

# 12.2.0
- Implement enchantment level specification for item selectors
    - See [examples here](https://github.com/dexman545/Fabric-Autoswitch/blob/master/fabric/src/gametest/resources/configs/enchantmentLevelTest.conf)

# 12.1.2
- Fix fabric loader dependency always being bumped when building
- Lock MC version upper bound to 1.21.8/25w36a due to breaking changes in vanilla

# 12.1.1
- Convert `feature-config.switch-away-from-tools` from a boolean to a set of actions that can 
have the switch away behavior. Defaults to just the `ATTACK` action.
- Fix switchback not triggering when `feature-config.switch-away-from-tools` selects an empty slot.
- Fix switchback remembering the incorrect slot

# 12.1.0
- Add config option `feature-config.switchback-waits-for-attack-progress` to
control when switchback waits for attack progress to fully reset.

# AutoSwitch 12.0.1
- Fix skipDeletedItems not being applied to items with durability

# Changes Since AutoSwitch 11
- Complete rewrite of AutoSwitch
- Update for 1.21.6
- Minecraft version range is no longer open ended
- Added Neoforge support
- New config system using HOCON, see config file in config/autoswitch.conf
  - Added target priorities for cases where multiple targets (eg tags) match
  - Support limiting entity targets based on equipment
  - Support ItemComponent selectors
    - Currently limited to Potion Contents
  - Removed custom tool groups, everything is now using item tags
  - Allow combing selectors with expressions such as AND, OR, XOR, etc for more complex selection logic
  - Use entity sensitive_to tags to for enchantment preferences
- Remove entity ride event default in favor of interact selectors
- Add default selector for interacting with fire -> water splash potion
- Add support for choosing what items to move to the offhand and for which action
- Fix DPS calculation for entity tool ratings
  - Swords are not correctly preferred over axs without explicit ordering
- Disable switchback when the player selects a new slot manually
- Rework AutoSwitch API
  - No longer requires a compile time dependency on AutoSwitch
    - On Neoforge, use IMC
    - On Fabric, use ObjectShare
- Use Minecraft's internal profiler in some places
- Added actual tests so hopefully things don't break
- Removed config commands
- emvoe bow target