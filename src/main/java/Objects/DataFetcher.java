package Objects;

import DB.DBControl;
import Servlets.ServletCompareSongs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import OutsourceDB.ABcontrol;
import OutsourceDB.MBcontrol;

public class DataFetcher {

    public static void getSongsMetaData(Song i_SongA, Song i_SongB) {
        JsonArray array = null;
        try {
            array = getJsonOfSong(i_SongA.getTitle(), i_SongA.getArtist());
        } catch (Exception ex) {
            Logger.getLogger(ServletCompareSongs.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (array != null) {
            i_SongA.getData().setHasData(true);
            i_SongA.getData().setHighLevelData(array.get(0));
            //i_SongA.getData().setLowLevelData(array.get(1));
        }
        JsonArray array2 = null;
        try {
            array2 = getJsonOfSong(i_SongB.getTitle(), i_SongB.getArtist());
        } catch (Exception ex) {
            Logger.getLogger(ServletCompareSongs.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (array2 != null) {
            i_SongB.getData().setHasData(true);
            i_SongB.getData().setHighLevelData(array2.get(0));
            //i_SongB.getData().setLowLevelData(array2.get(1));
        }
    }

    public static JsonArray ParseStringToJsonArray(String DataFromMBasStr) {
        JsonParser JP = new JsonParser();
        JsonElement DataFromMBasJsonElement = JP.parse(DataFromMBasStr);
        JsonObject DataFromMBasJsonObject = DataFromMBasJsonElement.getAsJsonObject();
        return (JsonArray) DataFromMBasJsonObject.get("recordings");
    }

    public static JsonElement ParseStringToJsonElement(String DataFromMBasStr) {

        JsonParser JP = new JsonParser();
        JsonElement DataFromMBasJsonElement = JP.parse(DataFromMBasStr);
        return DataFromMBasJsonElement;
    }

    public static JsonArray getJsonOfSong(String i_Title, String i_Artist) throws Exception {
        JsonArray result = new JsonArray();
        JsonArray MBdata = null;

        // Get the recording data from 'Music Brainz' as JsonArray
        try {
            MBdata = MBcontrol.GetDataFromMB(i_Title, i_Artist);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error in getJsonOfSong, Exception message: " + e.getMessage());
        }
        result = ABcontrol.getMBidFromAB(MBdata);
        return result;
    }

    static JsonElement getSongsMetaData(String i_Title, String i_Artist) {
        JsonArray array = null;
        JsonElement result = null;
        try {
            array = getJsonOfSong(i_Title, i_Artist);
            result = array.get(0);
        } catch (Exception ex) {
            Logger.getLogger(ServletCompareSongs.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return result;
    }
}
