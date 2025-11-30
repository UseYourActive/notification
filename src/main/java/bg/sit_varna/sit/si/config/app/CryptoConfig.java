package bg.sit_varna.sit.si.config.app;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.logging.Logger;

import java.security.Security;

@ApplicationScoped
public class CryptoConfig {

    private static final Logger LOG = Logger.getLogger(CryptoConfig.class);

    void onStart(@Observes StartupEvent ev) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            LOG.info("Registered Bouncy Castle Security Provider");
        }
    }
}
