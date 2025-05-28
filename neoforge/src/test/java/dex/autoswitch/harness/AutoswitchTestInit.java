package dex.autoswitch.harness;


import dex.autoswitch.CommonClass;
import dex.autoswitch.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID)
public class AutoswitchTestInit {
    // Must call into SwitchRegistry on initialization or
    // tests will fail due to classpath shenanigans
    public AutoswitchTestInit(IEventBus eventBus) {
        CommonClass.init();
    }
}
