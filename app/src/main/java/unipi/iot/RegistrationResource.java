package unipi.iot;

import org.eclipse.californium.core.CoapResource;

public class RegistrationResource extends CoapResource {

    public RegistrationResource(String name) {
        super(name);
    }

    public void handleGET(CoapExchange exchange) {
		exchange.accept();

		InetAddress inetAddress = exchange.getSourceAddress();
		CoapClient client = new CoapClient("coap://[" + inetAddress.getHostAddress() + "]:5683/.well-known/core");
		CoapResponse response = client.get();
		
		String code = response.getCode().toString();
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}

		String responseText = response.getResponseText();
		Integer startIndex = 0, endIndex;

		while (true) {
			startIndex = responseText.indexOf("</");
			if (startIndex == -1)
				break;
			endIndex = responseText.indexOf(">");
			String path = responseText.substring(startIndex + 2, endIndex);
			responseText = responseText.substring(endIndex + 1);

			if (path.contains("brightness")) {
				if(responseText.contains("obs")){
					Brightness brightness = new Brightness(path, inetAddress.getHostAddress());
					if (!Main.brightnesses.contains(brightness)) {
						Main.brightnesses.add(brightness);
						observe(brightness);
					}
				}
			} else if (path.contains("roller")) {
				Roller roller = new Roller(path, inetAddress.getHostAddress());
				if (!Main.rollers.contains(roller))
					Main.rollers.add(roller);
			}
		}
	}

	private static void observe(Brightness brightness) {
		MainApp.coapObserverClients.add(new CoapObserverClient(brightness));
		MainApp.coapObserverClients.get(MainApp.coapObserverClients.size() - 1).startObserving();
	}
}
