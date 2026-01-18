# 12.3.0
- Fix an error when writing data to the config file
- Remove unnecessary `OR` statements from the default config
- Allow eliding `type=<enchantment|item>` in `tools` list, offhand selectors, and `enchantments` definitions
- Fix usage of neoforge-replaced way of getting item enchantments
- Add a default target for ores
    - By default, prefers pickaxes with fortune over those with silk touch, unless the player is crouching,
      in which case the opposite occurs with silk touch being preferred.
- Ported Fabric API's "Client Tags" module to Neoforge
    - Now conventional tags (eg `c:ores`) will work when connecting to a vanilla server from a Neoforge client
- Fix Neoforge update url
- Add `Player` data type, mostly to support things like the mace
    - Usable in any `IdSelector` (anywhere you can have a `data` entry)
    - Supports the same sort of value comparison as Enchantment Levels
    - Example
      ```hocon
      target {
        type=BLOCK
        id="obsidian"
        data {
          player {
            isCrouching = true
            distance {
              GREATER = 2
            }
          }
        }
      }
      ```
    - Currently Available Entries

      | Type          | Description                                      | Type           |
      |---------------|--------------------------------------------------|----------------|
      | isFlying      | If the player is flying                          | Boolean        |
      | isCrouching   | If the player is crouching                       | Boolean        |
      | isPassenger   | If the player is riding something                | Boolean        |
      | isOnGround    | If the player is on the ground                   | Boolean        |
      | isSprinting   | If the player is sprinting                       | Boolean        |
      | distance      | The distance between the player and target       | Number         |
      | fallDistance  | The distance the player has fallen               | Number         |
      | hasItem       | If the player has the given item                 | ExpressionTree |
      | hasHotbarItem | If the player has the given item on their hotbar | ExpressionTree |

# 12.2.2
- Improve handling of fallback tools in default config
- Add mace as a preferred tool for destroying boats
- Fix error when writing an ExpressionTree to the config file
- Fix error when writing data to the config file
- Add support for "exploded data" - that is, the outer `data {}` tag is now optional. If present, it will take precedence
  over the implicitly defined `data`.
- Improve maintainability of the ToolSorter
- Remove unnecessary `OR` statements from the default config

# 12.2.1
- Remove shearing target defaults for mooshroom, bogged, and snow golems
- Add sea lanterns to the silk touch default target
- Add default config entry for things that prefer silk touch axes
- Add default config entry for things that prefer silk touch hoes
- Fix stat-change action not working in singleplayer
    - Unfortunately, due to how statistics work they cannot function in multiplayer.
        - If a suitable replacement is found, it will be added to get multiplayer working
- Simplify honey level target definition in default config
- Fix matching target debug texts being merged
- Fix `switch-actions` config not being obeyed

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