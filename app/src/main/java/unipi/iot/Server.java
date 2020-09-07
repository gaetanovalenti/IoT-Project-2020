package unipi.iot;

import org.eclipse.californium.core.CoapServer;

public class Server extends CoapServer {

    public void initServer() {
        System.out.println("Server initialized...");
        this.add(new RegistrationResource("registration"));
        this.start();
    }
}
