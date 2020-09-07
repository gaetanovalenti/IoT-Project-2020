package unipi.iot;

import org.eclipse.californium.core.CoapClient;

public class CoapObserverResource extends CoapClient {
    private Brightness brightness;
	CoapObserveRelation coapObserveRelation;

	public CoapObserverClient(Brightness brightness) {
		super(brightness.getResourceURI());
		this.brightness = brightness;
	}

	public void startObserving() {
		coapObserveRelation = this.observe(new CoapHandler () {
			public void onLoad(CoapResponse response) {
				try {
					String value;
					JSONObject jo = (JSONObject) JSONValue.parseWithException(response.getResponseText());
					Integer lowerThreshold = 200, upperThreshold = 700, index;
	
					if (jo.containsKey("brightness")) {
						value = jo.get("brightness").toString();
						Integer numericValue = Integer.parseInt(value.trim());
	
						if (numericValue < lowerThreshold) {
							index = Main.brightnesses.indexOf(brightness);
							Roller roller = Main.rollers.get(index);
							Boolean state = roller.getState();
							if (!state)
								roller.setState(false);
						}
	
						if (numericValue > upperThreshold) {
							index = Main.brightnesses.indexOf(brightness);
							Roller roller = Main.rollers.get(index);
							Boolean state = roller.getState();
							if (state) 
								roller.setState(true);							
						}
	
					} else {
						System.out.println("Brightness value not found.");
						return;
					}
	
					ArrayList<String> brightnessValues = brightness.getBrightnessValues();
					brightnessValues.add(value);
					Main.brightnesses.get(Main.brightnesses.indexOf(thermostat))
							.setBrightnessValues(brightnessValues);
	
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
	
			public void onError() {
				System.out.println("Error in observing.");
			}
		});
	}
}
