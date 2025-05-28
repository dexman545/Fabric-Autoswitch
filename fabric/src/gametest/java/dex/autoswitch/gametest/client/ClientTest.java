package dex.autoswitch.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

@SuppressWarnings("UnstableApiUsage")
public class ClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        System.out.println("Client test run");
    }
}
