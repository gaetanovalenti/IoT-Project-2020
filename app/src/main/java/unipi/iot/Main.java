package unipi.iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static ArrayList<CoapObserverResource> coapObserverClients = new ArrayList<CoapObserverResource>();
    public static ArrayList<Brightness> brightnesses = new ArrayList<Brightness>();
    public static ArrayList<Roller> rollers = new ArrayList<Roller>();

    public static void main(String[] args) throws IOException, InterruptedException {

        initServer();
        showOperations();

        while (true) {
            try {
                Scanner in = new Scanner(System.in);
                int selectedOperation = in.nextInt();
                
                Integer index;

                if(brightnesses.size() == 0 || rollers.size() == 0) 
					System.out.println("[WARNING] No registered resources yet");

                switch (selectedOperation) {
                    case 0:
                        showResources();
                        break;
                    case 1:
                        if ((index = getNode()) != null)
                            changeRollerState("ON", rollers.get(index), true);
                        break;
                    case 2:
                        if ((index = getNode()) != null)
                            changeRollerState("OFF", rollers.get(index), false);
                        break;
                    case 3:
                        resourcesInfo();
                        break;
                    case 4:
                        if ((index = getNode()) != null) {
                            String value = getLastObservedValue(brightnesses.get(index));
                            System.out.println("Last value registered: " + value);
                        }
                        break;
                    case 5:
                        if ((index = getNode()) != null)
                            singleResourceInfo(index);
                        break;
                    case 6:
                        System.exit(0);
                        break;
                    default:
                        showOperations();
                        break;
                }

            } catch (Exception e) {
                System.out.println("Invalid input. Try Again\n");
                showOperations();
                e.printStackTrace();
            }
        }
    }
    public static void initServer() {
        new Thread() {
            public void run() {
                Server server = new Server();
                server.initServer();
            }
        }.start();
    }

    public static void showOperations() {
        System.out.println("Commands List:");
        System.out.println("0: Show Resources");
        System.out.println("1: Open Roller Shutter");
        System.out.println("2: Close Roller Shutter");
        System.out.println("3: Resources Status");
        System.out.println("4: Last Brightness value Observed");
        System.out.println("5: Get Information about one node");
        System.out.println("6: exit");
    }

    public static void showResources() {
		System.out.println("List of the Resources in the system:");
		for (int i = 0; i < brightnesses.size(); i++) {
			Brightness brightness = brightnesses.get(i);
			Roller roller = rollers.get(i);
			System.out.println(
					+i + "\tBrightness: " + brightness.getAddress() + " " + brightness.getPath());
			System.out.println(+i + "\tRoller Shutter System: " + roller.getAddress() + " "
					+ roller.getPath() + Boolean.toString(roller.getState())+ " "+"\n");
        }
        System.out.println("\n -------------------------------------------- \n");
        System.out.println("\n -------------------------------------------- \n");
        showOperations();
	}

    public static Integer getNode() {
		System.out.print("Insert the node id: ");
        Scanner in = new Scanner(System.in);
        int index = in.nextInt();
		System.out.println();
		if (index == -1)
			return null;
		if (rollers.size() > index)
			return index;
		System.out.println("The selected node does not exists.");
		return null;
    }
    
    public static void changeRollerState(String state, Roller roller, Boolean mode) {
		CoapClient client = new CoapClient(roller.getResourceURI());
		CoapResponse response = client.post("state=" + state, MediaTypeRegistry.TEXT_PLAIN);
		String code = response.getCode().toString();
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		rollers.get(rollers.indexOf(roller)).setState(mode);
        System.out.println("Roller Shutter system switched to: " + state);
        System.out.println("\n -------------------------------------------- \n");
        System.out.println("\n -------------------------------------------- \n");
        showOperations();
    }
    
    public static String getLastObservedValue(Brightness brightness) {
		ArrayList<String> list = brightness.getBrightnessValues();
		if (list.isEmpty())
			return "N/A";
        return list.get(list.size() - 1);
    }
    
    public static void singleResourceInfo(Integer index) {
		Roller roller = rollers.get(index);
		String state = roller.getState() ? "ON" : "OFF";
		System.out.println(index + "\t" + roller.getAddress() + " " + roller.getPath()
				+ "\n\t\tState: " + state + "\n");

		Brightness brightness = brightnesses.get(index);
		System.out.println(index + "\t" + brightness.getAddress() + " " + brightness.getPath());
		ArrayList<String> list = brightness.getBrightnessValues();
		for (int j = 0; j < list.size(); ++j)
			System.out.println("\t\tId: " + j + "\tValue: " + list.get(j));
    }
    
    public static void resourcesInfo() {
		System.out.println("Information about the resources: \n");
		for (int i = 0; i <brightnesses.size(); ++i) {
			singleResourceInfo(i);
			System.out.println("\n -------------------------------------------- \n");
        }
        System.out.println("\n -------------------------------------------- \n");
        System.out.println("\n -------------------------------------------- \n");
        showOperations();
	}
}
