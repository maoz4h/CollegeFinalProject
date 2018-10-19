package OutsourceDB;

import Objects.DataFetcher;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ABcontrol {
	private final static String USER_AGENT = "Mozilla/5.0";
	private final static boolean debugMode = false;

	public static JsonElement getHighLevelData(String i_Title, String i_Artist, String i_Album) {
		JsonElement result = null;
		
		return result;
	}

	public static JsonArray getMBidFromAB(JsonArray DataFromMBasJsonArray) {
		JsonArray HighLevel = null;
		for (JsonElement jsonElement : DataFromMBasJsonArray) {
			JsonObject RecordingsAsJsonObject = jsonElement.getAsJsonObject();
			String CurrentId = RecordingsAsJsonObject.get("id").getAsString();
			try {
				HighLevel = GetDataFromAB(CurrentId);
				if (!HighLevel.get(0).isJsonNull()) {
					return HighLevel;
				}
			} catch (Exception e) {
				if (debugMode) {
					System.out.println("ABControl: There is no match for this search in Acoustic Brainz!");
					e.printStackTrace();
				}
				return null;
			}
		}
		if (debugMode) {
			System.out.println("ABControl: There is no match for this search in Acoustic Brainz!");
		}
		return null;
	}

	private static JsonArray GetDataFromAB(String i_ID) throws Exception {
		JsonArray result = new JsonArray();

		try {
			result.add(GetLevelFromABasJsonElement(i_ID, true));
			//result.add(GetLevelFromABasJsonElement(i_ID, false));
		} catch (Exception e) {
			if (debugMode) {
				System.out.println("Error in GetHighLevelFromABasJsonElement, Exception message: " + e.getMessage());
			}
		}

		return result;
	}

	private static JsonElement GetLevelFromABasJsonElement(String i_ID, boolean i_isHighLevel) {
		StringBuilder JsonAsStr = new StringBuilder();
		InputStream in = null;

		try {
			String urlStr = null;
			if (i_isHighLevel) {
				urlStr = "http://acousticbrainz.org/" + i_ID + "/high-level";
			} else {
				urlStr = "http://acousticbrainz.org/" + i_ID + "/low-level";
			}
			in = new URL(urlStr).openStream();
			URL url = new URL(urlStr);
			URLConnection c = url.openConnection();
			c.connect();
			in = c.getInputStream();
			byte[] buffer = new byte[4096];
			int bytesCount = 0;

			while ((bytesCount = in.read(buffer)) != -1) {
				String str = new String(buffer, 0, bytesCount);
				JsonAsStr.append(str);
			}

		} catch (Exception e) {
			if (i_isHighLevel && debugMode) {
				System.out.println("Error in GetLevelFromABasJsonElement (high), Exception message: " + e.getMessage());
			} else if (!i_isHighLevel && debugMode) {
				System.out.println("Error in GetLevelFromABasJsonElement (low), Exception message: " + e.getMessage());
			}
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				;
			}
		}
		try {
			// create a Json from JsonAsStr
			return DataFetcher.ParseStringToJsonElement(JsonAsStr.toString());
		} catch (Exception e) {
			if (i_isHighLevel && debugMode) {
				System.out.println("Error in GetLevelFromABasJsonElement (high) when parsing, Exception message: "
						+ e.getMessage());
			} else if (!i_isHighLevel && debugMode) {
				System.out.println("Error in GetLevelFromABasJsonElement (low) when parsing, Exception message: "
						+ e.getMessage());
			}
		}
		return null;
	}
}