package Objects;

import com.google.gson.JsonElement;
import java.util.Vector;

public class SongData {

    private JsonElement lowLevelData;
    private JsonElement highLevelData;
    private boolean hasData = false;
    private Vector<Double> highLevelVector = new Vector<Double>();

    public Vector<Double> getHighLevelVector() {
        return highLevelVector;
    }

    public void setHighLevelVector(Vector<Double> highLevelVector) {
        if (highLevelVector != null)
        {
            this.highLevelVector = highLevelVector;
            this.setHasData(true); 
        }
        
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public JsonElement getLowLevelData() {
        return lowLevelData;
    }

    public void setLowLevelData(JsonElement lowLevelData) {
        this.lowLevelData = lowLevelData;
    }

    public void setHighLevelData(JsonElement highLevelData) {
        this.highLevelData = highLevelData;
    }

    public JsonElement getHighLevelData() {
        return highLevelData;
    }
}
