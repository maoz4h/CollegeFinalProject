package BaseAlgorithim;

import DB.DBControl;
import Objects.Song;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author асу
 */
public class BaseInterface {
    public void enterSongsToComparisionDB(Song A,Song B, double num){
            
            enterComparisonScoresToDB(A,B,num);
        
    }
    
    
    public double getCompareScore(Vector<Double> SongA,Vector<Double> SongB) {
        AlgCalculations alg = new AlgCalculations();
        Vector<Double> compareVec = AlgCalculations.createCompareVector(SongA ,SongB);
        Vector<Double> formulaVec = alg.getFormulaVector();
        double result = 0;
        for(int i=0; i<compareVec.size(); i++){
            result += compareVec.get(i)*formulaVec.get(i);
        }
        return result;
    }

    private void enterComparisonScoresToDB(Song A, Song B, double num) {
        String title_a = A.getTitle();
        String artist_a = A.getArtist();
        String title_b = B.getTitle();
        String artist_b = B.getArtist();
        Vector<Double> compareVec = AlgCalculations.createCompareVector(A.getData().getHighLevelVector(), B.getData().getHighLevelVector());
        DBControl.insertCompareAlgorithmVector(title_a, artist_a, title_b, artist_b, compareVec, num);
            
    }
}


