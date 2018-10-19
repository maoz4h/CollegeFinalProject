/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

/**
 *
 * @author Maoz
 */
import DB.DBControl;
import static DB.DBControl.getInstance;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;

public class Comparison implements Serializable {

    private static final long serialVersionUID = 1L;

    //Song A
    private Song m_SongA;

    //Song B 	
    private Song m_SongB;

    private double m_ComparisonScore;

    //C'tors
    public Comparison() {
    }

    public Comparison(String i_SongAtitle, String i_SongAartist, String i_SongBtitle, String i_SongBartist, Double i_ComparisonScore) {
        //if it is an existing comparison from the DB - the i_ComparisonScore is different than -1
        //if it is not exist - it should be -1
        if (i_SongAtitle.compareToIgnoreCase(i_SongBtitle) < 0) {
            m_SongA = new Song(i_SongAtitle, i_SongAartist, true);
            m_SongB = new Song(i_SongBtitle, i_SongBartist, true);
        } else {
            m_SongA = new Song(i_SongBtitle, i_SongBartist, true);
            m_SongB = new Song(i_SongAtitle, i_SongAartist, true);
        }
        if (i_ComparisonScore != -1.0) {
            this.setM_ComparisonScore(i_ComparisonScore);
        } else {
            compareAndSaveComparison();
        }
    }

    public Comparison(Song i_SongA, Song i_SongB) {
        if (i_SongA.getTitle().compareToIgnoreCase(i_SongB.getTitle()) < 0) {
            m_SongA = i_SongA;
            m_SongB = i_SongB;
        } else {
            m_SongA = i_SongB;
            m_SongB = i_SongA;
        }

        compareAndSaveComparison();
    }

    //Getters and Setters
    public String getM_SongAtitle() {
        return m_SongA.getTitle();
    }

    public void setM_SongAtitle(String i_SongAtitle) {
        this.m_SongA.setTitle(i_SongAtitle);
    }

    public String getM_SongAartist() {
        return m_SongA.getArtist();
    }

    public void setM_SongAartist(String i_SongAartist) {
        this.m_SongA.setArtist(i_SongAartist);
    }

    public String getM_SongBtitle() {
        return m_SongB.getTitle();
    }

    public void setM_SongBtitle(String i_SongBtitle) {
        this.m_SongB.setTitle(i_SongBtitle);
    }

    public String getM_SongBartist() {
        return m_SongB.getArtist();
    }

    public void setM_SongBartist(String i_SongBartist) {
        this.m_SongB.setArtist(i_SongBartist);
    }

    public double getM_ComparisonScore() {
        return m_ComparisonScore;
    }

    public void setM_ComparisonScore(double m_ComparisonScore) {
        this.m_ComparisonScore = m_ComparisonScore;
    }

    public Song getSongA() {
        return m_SongA;
    }

    public Song getSongB() {
        return m_SongB;
    }

    //Valid Check
    public boolean areValidInputs() {
        boolean isValid = false;
        if (!(m_SongA.getTitle().isEmpty() || m_SongA.getTitle() == null
                || m_SongA.getArtist().isEmpty() || m_SongA.getArtist() == null
                || m_SongB.getTitle().isEmpty() || m_SongB.getTitle() == null
                || m_SongB.getArtist().isEmpty() || m_SongB.getArtist() == null)) {
            isValid = true;
        }
        return isValid;
    }

    //Compare and save songs
    private void compareAndSaveComparison() {
        if (areValidInputs()) {
            Double comparisonScore = isComparisonExists();
            if (comparisonScore == -1.0) {
                try {
                    setM_ComparisonScore(getSongA().closenessOfHighLevelVectors(getSongB()));

                    //save comparison in DB
                    saveComparisonInDB();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                m_ComparisonScore = comparisonScore;
            }
        }
    }

    private Double isComparisonExists() {
        DBControl dbControl = DBControl.getInstance();
        return dbControl.isComparisonExists(this);
    }

    private void saveComparisonInDB() {
        DBControl dbControl = DBControl.getInstance();
        dbControl.insertComparison(this);
    }

    public void addComparisonToFile(File newPlaylist) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Double getComparisonScoreFromDB() {
        DBControl dbControl = DBControl.getInstance();
        return dbControl.getComparisonScore(m_SongA, m_SongB);
    }

}
