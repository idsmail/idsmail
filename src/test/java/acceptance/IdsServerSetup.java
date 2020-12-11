package acceptance;

import com.icegreen.greenmail.util.ServerSetup;

class IdsServerSetup {
    static ServerSetup extendTo(ServerSetup serverSetup) {
        serverSetup.setServerStartupTimeout(2000);
        return serverSetup;
    }
}
