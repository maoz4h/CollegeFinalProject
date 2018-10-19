package Objects;

import BaseAlgorithim.BaseInterface;
import DB.DBControl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Song {

    private String m_Title;
    private String m_Artist;
    private SongData m_Data = new SongData();
    private String m_Path = null;
    private int m_Runtime = -1;

    public Song() {
    }
    
    Song(String i_Title, String i_Artist, String i_Path, int i_Runtime, boolean i_IsExists) {
        m_Title = i_Title;
        m_Artist = i_Artist;
        m_Path = i_Path;
        m_Runtime = i_Runtime;
        DBControl dbControl = DBControl.getInstance();

        //check if song in DB
        if (i_IsExists == false)
        {
            if (dbControl.isSongExists(m_Title, m_Artist)) {
                //if it is - get data from db
                i_IsExists = true;
                Vector<Double> highLevelMetaData = dbControl.getSongMetaData(m_Title, m_Artist);
                m_Data.setHighLevelVector(highLevelMetaData);
                m_Data.setHasData(true);
            }
            else
            {
                //if not - get meta data from AB and save in the DB
                this.getMetaDataFromAB();
                this.saveToDB();
            }
        }
        else {
            //if not - get meta data from AB and save in the DB
            this.getMetaDataFromAB();
            this.saveToDB();
        }
    }

    public Song(String i_Title, String i_Artist, boolean i_IsExists) {
        m_Title = i_Title;
        m_Artist = i_Artist;
        DBControl dbControl = DBControl.getInstance();

        if (i_IsExists == false)
        {
            //check if song in DB
            if (dbControl.isSongExists(m_Title, m_Artist)) {
                //if it is - get data from db
                i_IsExists = true;
                Vector<Double> highLevelMetaData = dbControl.getSongMetaData(m_Title, m_Artist);
                m_Data.setHighLevelVector(highLevelMetaData);
                m_Data.setHasData(true);
            }
            else
            {
                //if not - get meta data from AB and save in the DB
                this.getMetaDataFromAB();
                this.saveToDB();
            }
        }
        else
        {
            //if not - get meta data from AB and save in the DB
            this.getMetaDataFromAB();
            this.saveToDB();
        }
    }
    
    public Song(Row i_Row) {
        m_Title = i_Row.row.get(0).getKey().toString();
        m_Artist = i_Row.row.get(1).getKey().toString();
        m_Data = i_Row.ParseRowToData();
    }
    
    private void saveToDB() {
        if (m_Data.isHasData() == true)
        {
            DBControl dbControl = DBControl.getInstance();
            dbControl.insertSong(this);
        }
    }

    public String get_Path() {
        return m_Path;
    }

    public int get_Runtime() {
        return m_Runtime;
    }

    public void set_Path(String m_Path) {
        this.m_Path = m_Path;
    }

    public void set_Runtime(int m_Runtime) {
        this.m_Runtime = m_Runtime;
    }

    public double closenessOfHighLevelVectors(Song other) throws IOException {
        Vector<Double> thisSong = this.getData().getHighLevelVector();
        Vector<Double> otherSong = other.getData().getHighLevelVector();
        double closenessScore = 0;
        closenessScore = closenessOfVectors(thisSong, otherSong);
        return closenessScore;
    }

    private Vector<Double> placeHighLevelDataInVector(SongData data) throws IOException {
        JsonObject highLevObj = data.getHighLevelData().getAsJsonObject().get("highlevel").getAsJsonObject();
        JsonReader reader = new JsonReader(new StringReader(highLevObj.toString()));
        Vector<Double> toReturn = new Vector<Double>();
        Vector<Double> elem = getNextObjectAsPropertyVector(reader);
        toReturn.addAll(elem);
        return toReturn;
    }

    private Vector<Double> getNextObjectAsPropertyVector(JsonReader reader) throws IOException {
        reader.beginObject();
        Vector<Double> toReturn = new Vector<Double>();
        JsonToken nextTok = reader.peek();

        String name = null;

        while (nextTok != JsonToken.END_OBJECT) {
            if (nextTok == JsonToken.BEGIN_OBJECT) {
                toReturn.addAll(getNextObjectAsPropertyVector(reader));
            } else if (nextTok == JsonToken.NAME) {
                name = reader.nextName(); // maybe we will want to lookup which 
                //this is a specific skip for the "version" object ( we do not want to compare it)
                if (name.equals("version")) {
                    reader.skipValue();
                }
            } else if (nextTok == JsonToken.BOOLEAN) {
                reader.nextBoolean();
            } else if (nextTok == JsonToken.NULL) {
                reader.nextNull();
            } else if (nextTok == JsonToken.NUMBER) {
                toReturn.add(reader.nextDouble());
            } else if (nextTok == JsonToken.STRING) {
                reader.nextString();
            } else if (nextTok == JsonToken.BEGIN_ARRAY) {
                // no arrays in highlevel, maybe we will edit this later to include other types of json data
            } //shouldnt ever reach these tokens
            else if (nextTok == JsonToken.END_DOCUMENT) {
                return toReturn;
            } else if (nextTok == JsonToken.END_ARRAY) {
                reader.endArray();
            } else if (nextTok == JsonToken.END_OBJECT) {
                reader.endObject();
            }
            nextTok = reader.peek();
        }
        reader.endObject();
        return toReturn;
    }

    private Double getNextElementInVector(JsonReader reader) throws IOException {
        Double toReturn = (double) 0;
        JsonToken nextTok = reader.peek();
        String name = null;
        if (nextTok == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            toReturn = null;
        } else if (nextTok == JsonToken.NAME) {
            name = reader.nextName(); // maybe we will want to lookup which 
            toReturn = null;
        } else if (nextTok == JsonToken.BOOLEAN) {
            reader.nextBoolean();
            toReturn = null;
        } else if (nextTok == JsonToken.NULL) {
            reader.nextNull();
            toReturn = null;
        } else if (nextTok == JsonToken.NUMBER) {
            toReturn = reader.nextDouble();
        } else if (nextTok == JsonToken.STRING) {
            reader.nextString();
            toReturn = null;
        }
        return toReturn;
    }
    
    public SongData getData() {
        return m_Data;
    }

    public String getTitle() {
        return m_Title;
    }

    public void setTitle(String title) {
        this.m_Title = title;
    }

    public String getArtist() {
        return m_Artist;
    }

    public void setArtist(String artist) {
        this.m_Artist = artist;
    }

    public void getMetaDataAndSaveSong() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void getMetaDataFromAB() {
        m_Data.setHighLevelData(DataFetcher.getSongsMetaData(m_Title, m_Artist));
        Vector<Double> highLevelMetaData;
        DBControl dbControl = DBControl.getInstance();
        if (m_Data.getHighLevelData() != null) {
            try {
                highLevelMetaData = placeHighLevelDataInVector(m_Data);
                m_Data.setHighLevelVector(highLevelMetaData);
                m_Data.setHasData(true);
            } catch (IOException ex) {
                Logger.getLogger(Song.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private double closenessOfVectors(Vector<Double> thisSong, Vector<Double> otherSong) {
        /*double topside = 0;
        double botside = 0;
        double toReturn = 0;
        double tempThis = 0;
        double tempOther = 0;
        for (int i = 0; i < thisSong.size(); i++) {
            topside += thisSong.get(i) * otherSong.get(i);
        }
        for (int i = 0; i < thisSong.size(); i++) {
            tempThis += Math.pow(thisSong.get(i), 2);
            tempOther += Math.pow(otherSong.get(i), 2);
        }
        botside = Math.pow(tempThis, 0.5) * Math.pow(tempOther, 0.5);

        if (botside != 0) {
            toReturn = topside / botside;
        }
        return toReturn;*/
        BaseInterface bi = new BaseInterface();
        return bi.getCompareScore(thisSong, otherSong);
    }
}
