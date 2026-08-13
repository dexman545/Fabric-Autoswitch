# AutoSwitch ![Java CI](https://github.com/dexman545/Fabric-Autoswitch/workflows/Java%20CI/badge.svg)
![client](https://img.shields.io/badge/Environment-Client-1976d2?style=for-the-badge)
[![Fabric API](https://img.shields.io/badge/Requires-Fabric%20API-purple?style=for-the-badge)](https://modrinth.com/mod/fabric-api "Download Fabric API")

AutoSwitch is a Minecraft mod available on Fabric and NeoForge for automatically switching the active tool to the correct one
when attacking or interacting with blocks or entities - you attack a stone block, it switches to a pickaxe, 
then when you hit an annoying gravel patch it switches to a shovel. Which tool is chosen and when is all configurable, 
taking into account information such as the current enchantments on the tool, the target's blockstate, 
or an entity's equipment.

AutoSwitch uses a HOCON config file `autoswitch.conf`, located in the `config` folder which is next to the
resource pack folder. A quick way to navigate there is by going to `Options > Resource Packs > navigate up one folder`.

The mod can be toggled on/off in game by pressing `R` (default).

---

## Configuration

> This section only applies to AutoSwitch 12+. Earlier versions used a different config format.

There are four sections in the config file - three for defining selectors and targets when various actions occur,
and one for general feature configuration:

* `attack-action` - rules used when **attacking** (entities or blocks).
* `interact-action` - rules used when **interacting** (right-clicking entities or blocks).
* `stat-change-action` - rules triggered by stat events (e.g., a totem of undying activates, works only in singleplayer).
* `feature-config` - Controls the overall behavior of the mod.

Each rule contains:

* `priority` - integer; higher values are given precedence what multiple targets match.
* `target` - an `IdSelector` of type `BLOCK`, `ENTITY`, `STAT`, or an `EXPRESSION` allowing the combination of other targets).
* `tools` - ordered array of `IdSelector`s or `EXPRESSION`s with the `ITEM` type to switch to,
  or be an empty list (to disable switching for that target).

`IdSelector`s can reference tags or specific explicit IDs (`#minecraft:stone`, `minecraft:ender_chest`). They have an
optional `data` field to allow matching against things like specific item enchantments or blockstates. 
The `player` data field can be applied to any type of selector, allowing matching based on the player's condition such 
as if they are crouching or a certain distance away from a target.

`EXPRESSION`s can have as many elements or as much depth as you wish, allowing for a great deal of control.
The default config covers a wide range of cases and should serve as a great basis for expanding.

### Live edits

Simply resave the config file while the game is running for it to be automatically reloaded.

### Configuration Details

#### Expressions

Expressions can be nested to any depth you desire.

Expressions support the following operations:
* `OR` - Match any one of its children
* `AND` - Match all of its children
* `NOT` - Match none of its children
* `XOR` - Match only one of its children

#### Data

Currently, the following data types are supported:
* `BlockState` - The block state to match
* `Component` - Currently only supports `POTION_CONTENTS` on items
* `Enchantment` - The enchantments an item has
* `EntityEquipment` - The equipment an entity is wearing, such as a pig with a saddle
* `Player` - The player's condition, such as if they are sneaking, have a certain item, are riding something, or a 
 certain distance away from a target. 

#### Selection Priority

First, AutoSwitch will examine the target for any match, prefering the highest priority match with matching tools. 
If there are multiple matches with the same priority, it will begin to look at how specific the configuration is - 
for example, if config defines targets `#minecraft:stone` (a tag) and `minecraft:stone` (a block), and both match and
have the same priority, it will prefer the non-tag `minecraft:stone` target. If that target did not have any 
matching tools, then it will look at the tools specified by the tag target. If any optional data is specified, 
that is also considered to make the match more specific. The more data is specified, the more specific the 
match is considered.

If the matched targets have the same priority and specificity, then it will use a context-aware type rating to 
prioritize the selection. For blocks, they all have the same rating, but enchantments will be based on the enchantment 
level, or for items when attacking a mob, it will be based on the estimated damage to be dealt. Specified data can also 
have its own ratings (e.g. enchantments on a tool). The rating of the type takes precedence over the data rating.

Once a target has been selected, tool selection follows roughly the same process as above. A tool's priority is 
based on how early it appears in the tool list for the given target, otherwise the specificity, type rating, 
and data rating are the same as with targets.

If there is no preferred tool at this point, and the held item is one of these tying tools, then it will be 
preferred. Otherwise, it will prefer the tied tool in the leftmost hotbar slot. 

##### Text-based Flowchart
```
START: candidate inventory slots (all slots that matched a selector)
|
v
Compare Target Priority
|-- if different --> choose highest Target Priority --> END
|
v (same)
Compare Target Rating (levels 0..N)
For each level:
- compare: isGroup? (non-group preferred)
- compare: hasData? (with data preferred)
- compare: typeRating (higher wins)
- compare: dataRating (higher wins)
|-- if difference found --> choose winner --> END
|
v (no difference)
Compare Tool Priority
|-- if different --> choose highest Tool Priority --> END
|
v (same)
Compare Tool Rating (levels 0..N)
(same per-level rules as Target Rating)
|-- if difference found --> choose winner --> END
|
v (still tied)
Prefer currently selected slot?
|-- Yes --> choose currently selected --> END
|-- No  --> choose smallest (leftmost) slot --> END
```