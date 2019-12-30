package autoswitch;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDirMats}"})
public interface AutoSwitchMaterialConfig extends Config, Reloadable, Accessible {
    @Separator(",")
    @DefaultValue("wood, plant, replaceable_plant, pumpkin")
    MaterialHandler[] axeTargetMaterials();

    @Separator(",")
    @DefaultValue("wood, plant, replaceable_plant, pumpkin")
    MaterialHandler[] axeFortuneTargetMaterials();

    @Separator(",")
    @DefaultValue("egg, glass, ice, packed_ice, wood, pumpkin")
    MaterialHandler[] axeSilkTargetMaterials();

    @Separator(",")
    @DefaultValue("ice, packed_ice, metal, anvil, shulker_box, stone, redstone_lamp")
    MaterialHandler[] pickTargetMaterials();

    @Separator(",")
    @DefaultValue("stone, glass, metal")
    MaterialHandler[] pickFortuneTargetMaterials();

    @Separator(",")
    @DefaultValue("egg, glass, ice, packed_ice, stone")
    MaterialHandler[] pickSilkTargetMaterials();

    @Separator(",")
    @DefaultValue("plant, unused_plant, underwater_plant, replaceable_plant, leaves, cobweb, wool")
    MaterialHandler[] shearTargetMaterials();

    @Separator(",")
    @DefaultValue("bamboo, bamboo_sapling, pumpkin, cobweb, leaves")
    MaterialHandler[] swordTargetMaterials();

    @Separator(",")
    @DefaultValue("earth, organic, snow, snow_block, clay, sand")
    MaterialHandler[] shovelTargetMaterials();

    @Separator(",")
    @DefaultValue("egg, glass, ice, packed_ice, earth,organic, sand, snow, snow_block")
    MaterialHandler[] shovelSilkTargetMaterials();



}
