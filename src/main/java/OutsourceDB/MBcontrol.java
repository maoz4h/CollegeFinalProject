package OutsourceDB;

import Objects.DataFetcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonArray;

public class MBcontrol {
	private final static String USER_AGENT = "Mozilla/5.0";
	
	public static JsonArray GetDataFromMB(String title, String artist) throws Exception {
		title = '"' + title + '"';
		artist = '"' + artist + '"';
		
		String url1 = "http://musicbrainz.org/ws/2/recording/?query=";
		String url2 = URLEncoder.encode(title + " artist:" + artist, "UTF-8");
		
		URL obj = new URL(url1 + url2 + ";fmt=json");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		int responseCode = con.getResponseCode();
		for (int i=0; i < 10 && (responseCode != 200 && responseCode != 404); i++)
		{
			con.disconnect();
			con.connect();
			con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			responseCode = con.getResponseCode();
		}
		if (responseCode == 200)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//return response.toString();
			return DataFetcher.ParseStringToJsonArray(response.toString());
		}
		else
		{
			System.out.println("There is no match in Music Brainz for this search.");
			return null;
		}
	}
}

