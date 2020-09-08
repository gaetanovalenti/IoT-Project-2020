package unipi.iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class CoapObserverResource extends CoapClient {
    private Brightness brightness;
	CoapObserveRelation coapObserveRelation;

	public CoapObserverResource(Brightness brightness) {
		super(brightness.getResourceURI());
		this.brightness = brightness;
	}



    public void startObserving() {
		coapObserveRelation = this.observe(new CoapHandler() {
			public void onLoad(CoapResponse response) {
				try {
					String value;
					String code = response.getCode().toString();
					if(code.startsWith("2")) {
						JSONObject jo = (JSONObject) JSONValue.parseWithException(response.getResponseText());
						Integer lowerThreshold = 200, upperThreshold = 700, index;

						if (jo.containsKey("brightness")) {
							value = jo.get("brightness").toString();
							Integer numericValue = Integer.parseInt(value.trim());
							System.out.println("observed value of brightness is" + numericValue);

							if (numericValue < lowerThreshold) {
								index = Main.brightnesses.indexOf(brightness);
								Roller roller = Main.rollers.get(index);
								Boolean state = roller.getState();
								if (state) {
									System.out.println("observed state of roller is" + state);
									roller.setState(false);
									System.out.println("observed state of roller is" + roller.getState());
								}
							}
							if (numericValue > upperThreshold) {
								index = Main.brightnesses.indexOf(brightness);
								Roller roller = Main.rollers.get(index);
								Boolean state = roller.getState();
								if (!state) {
									System.out.println("observed state of roller is" + state);
									roller.setState(true);
									System.out.println("observed state of roller is" + roller.getState());
								}
							}

						} else {
							System.out.println("Brightness value not found.");
							return;
						}

						ArrayList<String> brightnessValues = brightness.getBrightnessValues();
						brightnessValues.add(value);
						Main.brightnesses.get(Main.brightnesses.indexOf(brightness))
								.setBrightnessValues(brightnessValues);
					} else {
						System.err.println("[OBSERVING HANDLER] Response Code "+code);
					}
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
