# AutoSwitch ![Java CI](https://github.com/dexman545/Fabric-Autoswitch/workflows/Java%20CI/badge.svg)
AutoSwitch is a mod for Minecraft using the Fabric mod loader. It is designed to automatically change the player's tool 
to the correct one when it is being used.

## Configuration

All provided config entries are documented in-place by the preceding lines that begin with a `#`.
The default values of those entries are also provided there. The config files are reloaded upon being saved,
allowing for quick testing and modification.

All files may be found in the config folder.
### Features (`autoswitch.cfg`)
This config controls AutoSwitch's behavior in various aspects outside of simple tool selection.
These options are available for in-game modification via the client commands, try with `/autoswitch` in-game.

### Tool Selection (`autoswitchAttackAction.cfg` and `autoswitchUseAction.cfg`)
These are config entries defined in the format of `Target = List of ToolSelectors`.

The list of `ToolSelector`s is comma-separated and ordered. Leave blank/empty to disable switching for that target.

In placed where an id (`Identifier`) is needed, all colons (`:`) must be replaced with an exclamation mark (`!`) for 
targets, and may be done for `ToolSelector`s.

#### Target
A target can be one of three things: 
  - a provided named target or target group, such as a `Material` or `EntityGroup`,
  - a specific `Identifier` of a block or entity, such as `minecraft:stone`, or
  - a `TagSelector`.

All colons (`:`) must be replaced with an exclamation mark (`!`) for targets.

#### Tool Selector
A `ToolSelector` has 2 parts: an `ItemSelector`, and a list of `EnchantmentSelector`s. 
This two are separated by a semicolon (`;`).

The list of `EnchantmentSelector`s is optional. When excluded, no enchantments are required to select 
the accompanying item. The list of `EnchantmentSelector`s is ampersand-separated (`&`).

Note: tools will perform a check if they are effective for the target. You can disable this behavior in the 
features config via `miningLevelCheck`.

##### Item Selector
An `ItemSelector` is what matches an item in for selection. An `ItemSelector` may be one of three things:
  - a provided named tool group, such as `pickaxe` or `any`,
  - a specific `Identifier` that matches the tools `id`, or
  - a `TagSelector`.

##### Enchantment Selector
An `EnchantmentSelector` allows for only items with matching enchantments to be selected. An `EnchantmentSelector` may be one of three things:
- a provided named tool group, such as `pickaxe` or `any`,
- a specific `Identifier` that matches the tools `id`, or
- a `TagSelector`.
See `toolEnchantmentsStack` in the features config for differing enchantment behavior.

#### Tag Selector
`TagSelector`s rely on Minecraft's tags to function. While playing, these tags are provided by the server and are 
and can vary by world. As such, these selectors are inherently unreliable and are not expected to work on vanilla servers, 
and are therefor not provided in any default capacity by AutoSwitch.

A `TagSelector` has the form of `type@tag_id`. The available types are: `block`, `entity`, `item`, and `enchantment`.
The latter of which are only useful for `ToolSelector`s. The required type should be self explanatory.

The `tag_id` is the `Identifier` of the tag. For blocks, you can see this in the `F3` menu when looking at one.

You may control these per-world via datapacks. See the Minecraft Wiki for more information.

It is the responsibility of mod authors to correctly add their additions to the conventional tags.

##### Why is `type` required?
A couple of reasons:
  - Allows for it to be differentiated from selectors matching a particular id, and
  - Internally, Minecraft stores the game objects (eg. blocks) in a `Registry`, 
which maps an `Identifier` to the object. These `Identifiers` are only guaranteed to be unique within their `Registry`.

#### Examples 

To target normal stone with silk touch pickaxes:
`minecraft!stone = pickaxe;minecraft!silk_touch` or `minecraft!stone = item@c:pickaxes;minecraft!silk_touch`

To target all ores with fortune pickaxes:
`block@c:ores = pickaxe;minecraft!fortune` or `block@c:ores = pickaxe;enchantment@fortune`.

### Bundled Dependency License Notice
This mod makes use of the OWNER library for its configuration.
OWNER is [licensed under the BSD license](http://owner.aeonbits.org/docs/license/). A copy of this license can be found 
under assets/aswitch/OWNER-license.txt
