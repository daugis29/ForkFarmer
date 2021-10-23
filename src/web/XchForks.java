package web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import forks.Fork;
import main.MainGui;
import main.Settings;
import types.XchForksData;

public class XchForks {
	private static LocalDateTime lastUpdateATB;
	private static int UPDATE_RATE_SEC = 60*60;
	
	// Interacts with xchforks.com API. Thanks!
	public static void updatePrices() {
		if (null == lastUpdateATB)
			lastUpdateATB = LocalDateTime.now();
		else if (Duration.between(lastUpdateATB, LocalDateTime.now()).getSeconds() < UPDATE_RATE_SEC)
			return; // too early to update
		
		updatePricesForced();
	}
	
	public static void updatePricesForced() {
		lastUpdateATB = LocalDateTime.now();
		
		List<XchForksData> list = new ArrayList<>();
		
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
				
			    
	        String plainCredentials = "orfinkat:gqr7654pjn348c3u"; //did this so auth string not scraped
	        //for (char c : plainCredentials.toCharArray())
	        	//sb.append((char)(c-1));
	        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
	                String authorizationHeader = "Basic " + base64Credentials;
	        
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://xchforks.com/api/v1/listings"))
	                .timeout(Duration.ofSeconds(5))
	                .header("Authorization", authorizationHeader)
	                .build();
	
	        HttpResponse<String> response;
	        
	        
        	response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
        	
        	String jsonResponse = response.body();
        	
        	JSONParser parser = new JSONParser();
        	JSONArray jsonArray = (JSONArray) parser.parse(jsonResponse);
        	
        	for(Object o : jsonArray)
    			list.add(new XchForksData((JSONObject) o));
        	
        	for(XchForksData d : list) {
    			Fork.LIST.stream().filter(f -> f.symbol.equals(d.symbol)).forEach(f -> {
    				if (d.price > -1)
    					f.updatePrice(d.price * Settings.GUI.currencyRatio);
    				if (null != d.latestVersion && !d.latestVersion.equals("Unknown")) {
    					f.latestVersion = d.latestVersion;
    					f.published = d.published;
    				}
    			});
    		}
        	MainGui.updateTotal();
	        	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
