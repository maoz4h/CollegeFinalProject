/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

/**
 *
 * @author Maoz
 */
import Objects.Comparison;
import Objects.Song;
import Objects.Row;
import matrix.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.media.jfxmedia.control.VideoRenderControl;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import Objects.User;

import java.awt.Event;
import java.io.Console;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.ResolutionSyntax;
import org.apache.jasper.tagplugins.jstl.core.ForEach;

//import org.apache.jasper.tagplugins.jstl.core.If;
public class DBControl {

    private static DBControl singletonDB = null;
    private static Connection m_SQLConn = null;

    protected DBControl() {
    }

    public static DBControl getInstance() {
        if (singletonDB == null) {
            try {
                singletonDB = new DBControl();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return singletonDB;
    }

    public Connection getConnection() {
        if (m_SQLConn == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                m_SQLConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/FinalProject?useUnicode=true&characterEncoding=UTF-8", "root", "root");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return m_SQLConn;
    }

    public Statement getStatement(Connection i_Conn) {
        Statement stat = null;
        try {
            stat = i_Conn.createStatement();
        } catch (Exception ex) {
        }
        return stat;
    }

    //Users Queries
    public static User setNewUser(String i_Email, String i_Password, String i_Name) {
        //Manualy prepared statement text - saves time
        StringBuilder sb = new StringBuilder("insert into users (email, name, password) values (?, ?, ?)");
        //Auto prepared statement text
        //StringBuilder sb = getSBstatement();

        PreparedStatement insertUserStatement = null;
        try {
            insertUserStatement = getInstance().getConnection().prepareStatement(sb.toString());

            insertUserStatement.setString(1, i_Email);
            insertUserStatement.setString(2, i_Name);
            insertUserStatement.setString(3, i_Password);
            insertUserStatement.execute();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return new User(i_Email, i_Password, i_Name);
    }
    
    public static User getUserByEmail(String i_Email) {
        String GetUserByEmailQuery = "select * from users where email=?";
        User returnedUser = new User();
        try {
            PreparedStatement UserByEmail = getInstance().getConnection().prepareStatement(GetUserByEmailQuery);
            UserByEmail.setString(1, i_Email);
            UserByEmail.executeQuery();
            returnedUser = parseResultSetToUser(UserByEmail.getResultSet());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return returnedUser;
    }

    private static User parseResultSetToUser(ResultSet resultSet) {
        String email = null;
        String name = null;
        String password = null;
        User parsedUser = new User();
        try {
            while (resultSet.next()) {
                email = resultSet.getString("email");
                name = resultSet.getString("name").toString();
                password = resultSet.getString("password").toString();
                parsedUser = new User(email, password, name);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parsedUser;
    }

    public boolean isUserExistsByEmail(String i_Email) {

        boolean isUserExists = false;
        String isUserExistsQuery = "select * from users where email = ?";
        try {
            PreparedStatement isUserExistsStatement = getInstance().getConnection().prepareStatement(isUserExistsQuery);
            isUserExistsStatement.setString(1, i_Email);
            ResultSet rSet = isUserExistsStatement.executeQuery();

            if (rSet.first()) {
                isUserExists = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isUserExists;

    }

    public String GetUserPasswordByEmail(String i_Email) {

        return getUserByEmail(i_Email).getM_Password();
    }

    //Songs Queries
    public void insertSong(Song i_Song) {
        //Manualy prepared statement text - saves time
        StringBuilder sb = new StringBuilder("insert into songs (title, artist, danceable, not_danceable, danceability_probability, female, male, gender_probability, genre_alternative, genre_blues, genre_electronic, genre_folkcountry, genre_funksoulrnb, genre_jazz, genre_pop, genre_raphiphop, genre_rock, genre_probability, electronic_ambient, electronic_dnb, electronic_house, electronic_techno, electronic_trance, electronic_probability, rosamerica_cla, rosamerica_dan, rosamerica_hip, rosamerica_jaz, rosamerica_pop, rosamerica_rhy, rosamerica_roc, rosamerica_spe, rosamerica_probablity, tzanetakis_blu, tzanetakis_cla, tzanetakis_cou, tzanetakis_dis, tzanetakis_hip, tzanetakis_jaz, tzanetakis_met, tzanetakis_pop, tzanetakis_reg, tzanetakis_roc, tzanetakis_proababilty, ismir04_rhythm_ChaChaCha, ismir04_rhythm_Jive, ismir04_rhythm_Quickstep, ismir04_rhythm_Rumba_American, ismir04_rhythm_Rumba_International, ismir04_rhythm_Rumba_Misc, ismir04_rhythm_Samba, ismir04_rhythm_Tango, ismir04_rhythm_VienneseWaltz, ismir04_rhythm_Waltz, ismir04_rhythm_probability, mood_acoustic_acoustic, mood_acoustic_not_acoustic, mood_acoustic_probability, mood_aggressive_aggressive, mood_aggressive_not_aggressive, mood_aggressive_probability, mood_electronic_electronic, mood_electronic_not_electronic, mood_electronic_probability, mood_happy_happy, mood_happy_not_happy, mood_happy_probability, mood_party_not_party, mood_party_party, mood_party_probability, mood_relaxed_not_relaxed, mood_relaxed_relaxed, mood_relaxed_probability, mood_sad_not_sad, mood_sad_sad, mood_sad_probability, moods_mirex_Cluster1, moods_mirex_Cluster2, moods_mirex_Cluster3, moods_mirex_Cluster4, moods_mirex_Cluster5, moods_mirex_probability, timbre_bright, timbre_dark, timbre_probability, tonal_atonal_atonal, tonal_atonal_tonal, tonal_atonal_probability, voice_instrumental_instrumental, voice_instrumental_voice, voice_instrumental_probability) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        //Auto prepared statement text
        //StringBuilder sb = getSBstatement();

        PreparedStatement insertSongStatement = null;
        try {
            insertSongStatement = getInstance().getConnection().prepareStatement(sb.toString());

            insertSongStatement.setString(1, i_Song.getTitle());
            insertSongStatement.setString(2, i_Song.getArtist());

            Vector<Double> highLevelValues = i_Song.getData().getHighLevelVector();
            for (int i = 2; i < 91; i++) {
                insertSongStatement.setDouble(i + 1, highLevelValues.get(i - 2));
            }
            insertSongStatement.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void insertCompareAlgorithmVector(String SongATitle,String SongAArtist,String SongBTitle,String SongBArtist,Vector<Double> compareVec,double score) {
        StringBuilder sb = new StringBuilder("insert into compare_database (title_a, artist_a, title_b, artist_b, danceable, not_danceable, danceability_probability, female, male, gender_probability, genre_alternative, genre_blues, genre_electronic, genre_folkcountry, genre_funksoulrnb, genre_jazz, genre_pop, genre_raphiphop, genre_rock, genre_probability, electronic_ambient, electronic_dnb, electronic_house, electronic_techno, electronic_trance, electronic_probability, rosamerica_cla, rosamerica_dan, rosamerica_hip, rosamerica_jaz, rosamerica_pop, rosamerica_rhy, rosamerica_roc, rosamerica_spe, rosamerica_probablity, tzanetakis_blu, tzanetakis_cla, tzanetakis_cou, tzanetakis_dis, tzanetakis_hip, tzanetakis_jaz, tzanetakis_met, tzanetakis_pop, tzanetakis_reg, tzanetakis_roc, tzanetakis_proababilty, ismir04_rhythm_ChaChaCha, ismir04_rhythm_Jive, ismir04_rhythm_Quickstep, ismir04_rhythm_Rumba_American, ismir04_rhythm_Rumba_International, ismir04_rhythm_Rumba_Misc, ismir04_rhythm_Samba, ismir04_rhythm_Tango, ismir04_rhythm_VienneseWaltz, ismir04_rhythm_Waltz, ismir04_rhythm_probability, mood_acoustic_acoustic, mood_acoustic_not_acoustic, mood_acoustic_probability, mood_aggressive_aggressive, mood_aggressive_not_aggressive, mood_aggressive_probability, mood_electronic_electronic, mood_electronic_not_electronic, mood_electronic_probability, mood_happy_happy, mood_happy_not_happy, mood_happy_probability, mood_party_not_party, mood_party_party, mood_party_probability, mood_relaxed_not_relaxed, mood_relaxed_relaxed, mood_relaxed_probability, mood_sad_not_sad, mood_sad_sad, mood_sad_probability, moods_mirex_Cluster1, moods_mirex_Cluster2, moods_mirex_Cluster3, moods_mirex_Cluster4, moods_mirex_Cluster5, moods_mirex_probability, timbre_bright, timbre_dark, timbre_probability, tonal_atonal_atonal, tonal_atonal_tonal, tonal_atonal_probability, voice_instrumental_instrumental, voice_instrumental_voice, voice_instrumental_probability, compare_score) values (?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        //Auto prepared statement text
        //StringBuilder sb = getSBstatement();

        PreparedStatement insertSongStatement = null;
        try {
            insertSongStatement = getInstance().getConnection().prepareStatement(sb.toString());

            insertSongStatement.setString(1, SongATitle);
            insertSongStatement.setString(2, SongAArtist);
            insertSongStatement.setString(3, SongBTitle);
            insertSongStatement.setString(4, SongBArtist);
            insertSongStatement.setDouble(94,score);

            
            for (int i = 4; i < 93; i++) {
                insertSongStatement.setDouble(i + 1, compareVec.get(i - 4));
            }
            insertSongStatement.execute();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public Vector<Song> getAllSongs()
    {
        Vector<Song> allSongs = new Vector<>();
        String getAllSongsQuery = "select * from songs";
        
        try {
                PreparedStatement getSongStatement = getInstance().getConnection().prepareStatement(getAllSongsQuery);

                ResultSet rSet = getSongStatement.executeQuery();
                allSongs = parseResultSetToSongs(rSet);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        
        return allSongs;
    }
    
    private Song parseResultSetToAsingleSong(ResultSet i_rSet)
    {
        Song song = null;
        
        try {
            if (i_rSet != null) {
                List<Row> table = new ArrayList<Row>();
                Row.formTable(i_rSet, table);
                for (Row row : table) {
                    song = new Song(row);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        return song;
    }
    
    private Vector<Song> parseResultSetToSongs(ResultSet i_rSet)
    {
        Vector<Song> songs = new Vector<Song>();
        
        try {
            if (i_rSet != null) {
                List<Row> table = new ArrayList<Row>();
                Row.formTable(i_rSet, table);
                for (Row row : table) {
                    songs.add(new Song(row));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        return songs;
    }  

    public Vector<Double> getSongMetaData(String i_Title, String i_Artist) {
        Vector<Double> result = new Vector<Double>();
        if (isSongExists(i_Title, i_Artist)) {
            result = new Vector<Double>();
            String getSongQuery = "select * from songs where title = ? and artist = ?";
            try {
                PreparedStatement getSongStatement = getInstance().getConnection().prepareStatement(getSongQuery);
                getSongStatement.setString(1, i_Title);
                getSongStatement.setString(2, i_Artist);

                ResultSet rSet = getSongStatement.executeQuery();
                result = parseResultSetToAsingleSong(rSet).getData().getHighLevelVector();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    //Comparisons Queries
    public Vector<Comparison> getComparisons() {
        Vector<Comparison> comparisons = new Vector<Comparison>();
        String sqlGetComparisons = "select * from comparisons";
        try {
            PreparedStatement comparisonsStatement = null;
            try {
                comparisonsStatement = getInstance().getConnection().prepareStatement(sqlGetComparisons);

            } catch (SQLException ex1) {
                Logger.getLogger(DBControl.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
            try {
                comparisonsStatement.executeQuery();

            } catch (SQLException ex1) {
                Logger.getLogger(DBControl.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }

            comparisons = parseResultSetToComparisons(comparisonsStatement.getResultSet());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return comparisons;
    }

    public Double isComparisonExists(Comparison i_Comparison) {
//if comparison exists -> returns the comparison score
//else -> returns -1.0
        Double comparisonScore = -1.0;
        String isComparisonExistsQuery = "select * from comparisons where song_a_title = ? and song_a_artist = ? and song_b_title = ? and song_b_artist = ?";
        try {
            PreparedStatement isComparisonExistsStatement = getInstance().getConnection().prepareStatement(isComparisonExistsQuery);
            isComparisonExistsStatement.setString(1, i_Comparison.getM_SongAtitle());
            isComparisonExistsStatement.setString(2, i_Comparison.getM_SongAartist());
            isComparisonExistsStatement.setString(3, i_Comparison.getM_SongBtitle());
            isComparisonExistsStatement.setString(4, i_Comparison.getM_SongBartist());

            ResultSet rSet = isComparisonExistsStatement.executeQuery();
            comparisonScore = parseResultSetToComparisonScore(rSet);    //if the comparison exists -> the comparisonScore is != -1.0
            if (comparisonScore != -1.0)    //if comparison exists
            {
                return comparisonScore;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return comparisonScore;
    }

    //Comparisons Actions
    private Double parseResultSetToComparisonScore(ResultSet i_ResultSet) {
        
        Double comparisonScore = -1.0;

        try {
            while (i_ResultSet.next()) {
                try {
                    comparisonScore = i_ResultSet.getDouble("comparison_score");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return comparisonScore;
    }
    
    private Vector<Comparison> parseResultSetToComparisons(ResultSet i_ResultSet) {
        Vector<Comparison> parsedComparisons = new Vector<Comparison>();

        String songAtitle = null;
        String songAartist = null;
        String songBtitle = null;
        String songBartist = null;
        Double comparisonScore = -1.0;

        try {
            while (i_ResultSet.next()) {
                try {
                    songAtitle = i_ResultSet.getString("song_a_title").toString();
                    songAartist = i_ResultSet.getString("song_a_artist").toString();
                    songBtitle = i_ResultSet.getString("song_b_title").toString();
                    songBartist = i_ResultSet.getString("song_b_artist").toString();
                    comparisonScore = i_ResultSet.getDouble("comparison_score");
                    Comparison comparison = new Comparison(songAtitle, songAartist, songBtitle, songBartist, comparisonScore);
                    parsedComparisons.add(comparison);
                } catch (SQLException e) {
                    e.printStackTrace();

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return parsedComparisons;
    }

    private Comparison parseResultSetToComparison(ResultSet i_ResultSet) {
        Comparison parsedComparison = null;

        String songAtitle = null;
        String songAartist = null;
        String songBtitle = null;
        String songBartist = null;
        Double comparisonScore = -1.0;

        try {
            while (i_ResultSet.next()) {
                try {
                    songAtitle = i_ResultSet.getString("song_a_title").toString();
                    songAartist = i_ResultSet.getString("song_a_artist").toString();
                    songBtitle = i_ResultSet.getString("song_b_title").toString();
                    songBartist = i_ResultSet.getString("song_b_artist").toString();
                    comparisonScore = i_ResultSet.getDouble("comparison_score");
                    parsedComparison = new Comparison(songAtitle, songAartist, songBtitle, songBartist, comparisonScore);
                } catch (SQLException e) {
                    e.printStackTrace();

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBControl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return parsedComparison;
    }

    public void insertComparison(Comparison i_Comparison) {
        String sqlInsertComparison = "insert into comparisons (song_a_title, song_a_artist, song_b_title, song_b_artist, comparison_score) values(?,?,?,?,?)";

        if (!(i_Comparison.getM_SongAtitle().equalsIgnoreCase(i_Comparison.getM_SongBtitle())
                && i_Comparison.getM_SongAartist().equalsIgnoreCase(i_Comparison.getM_SongBartist()))) {
            try {
                PreparedStatement InsertComparison = getInstance()
                        .getConnection().prepareStatement(sqlInsertComparison);
                InsertComparison.setString(1, i_Comparison.getM_SongAtitle());
                InsertComparison.setString(2, i_Comparison.getM_SongAartist());
                InsertComparison.setString(3, i_Comparison.getM_SongBtitle());
                InsertComparison.setString(4, i_Comparison.getM_SongBartist());
                InsertComparison.setDouble(5, i_Comparison.getM_ComparisonScore());
                InsertComparison.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Double getComparisonScore(Song i_SongA, Song i_SongB) {
        Double comparisonScore = 0.0;
        String getComparisonQuery = "select * from comparisons where song_a_title = ? and song_a_artist = ? and song_b_title = ? and song_b_artist = ?";
        try {
            PreparedStatement getComparisonStatement = getInstance().getConnection().prepareStatement(getComparisonQuery);
            getComparisonStatement.setString(1, i_SongA.getTitle());
            getComparisonStatement.setString(2, i_SongA.getArtist());
            getComparisonStatement.setString(3, i_SongB.getTitle());
            getComparisonStatement.setString(4, i_SongB.getArtist());

            ResultSet rSet = getComparisonStatement.executeQuery();
            Comparison parsedComparison = parseResultSetToComparison(rSet);
            comparisonScore = parsedComparison.getM_ComparisonScore();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return comparisonScore;
    }

    private StringBuilder getSBstatement() {
        Vector<String> colsNames = new Vector<String>();
        StringBuilder sb = new StringBuilder();
        DatabaseMetaData dbMeta;
        try {
            dbMeta = getInstance().getConnection().getMetaData();
            ResultSet res = dbMeta.getColumns(null, null, "songs", null);
            while (res.next()) {
                colsNames.add(res.getString("COLUMN_NAME"));
            }
            res.close();
            sb.append("insert into songs (");
            for (int i = 0; i < 91; i++) {
                if (i != 90) {
                    sb.append(colsNames.get(i) + ", ");
                } else {
                    sb.append(colsNames.get(i) + ") ");
                }
            }
            sb.append("values (");
            for (int i = 0; i < 91; i++) {
                if (i != 90) {
                    sb.append("?, ");
                } else {
                    sb.append("?)");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb;

    }

    public static void insertFormula(Vector<Double> formula) {
        StringBuilder sb = new StringBuilder("insert into formula (danceable, not_danceable, danceability_probability, female, male, gender_probability, genre_alternative, genre_blues, genre_electronic, genre_folkcountry, genre_funksoulrnb, genre_jazz, genre_pop, genre_raphiphop, genre_rock, genre_probability, electronic_ambient, electronic_dnb, electronic_house, electronic_techno, electronic_trance, electronic_probability, rosamerica_cla, rosamerica_dan, rosamerica_hip, rosamerica_jaz, rosamerica_pop, rosamerica_rhy, rosamerica_roc, rosamerica_spe, rosamerica_probablity, tzanetakis_blu, tzanetakis_cla, tzanetakis_cou, tzanetakis_dis, tzanetakis_hip, tzanetakis_jaz, tzanetakis_met, tzanetakis_pop, tzanetakis_reg, tzanetakis_roc, tzanetakis_proababilty, ismir04_rhythm_ChaChaCha, ismir04_rhythm_Jive, ismir04_rhythm_Quickstep, ismir04_rhythm_Rumba_American, ismir04_rhythm_Rumba_International, ismir04_rhythm_Rumba_Misc, ismir04_rhythm_Samba, ismir04_rhythm_Tango, ismir04_rhythm_VienneseWaltz, ismir04_rhythm_Waltz, ismir04_rhythm_probability, mood_acoustic_acoustic, mood_acoustic_not_acoustic, mood_acoustic_probability, mood_aggressive_aggressive, mood_aggressive_not_aggressive, mood_aggressive_probability, mood_electronic_electronic, mood_electronic_not_electronic, mood_electronic_probability, mood_happy_happy, mood_happy_not_happy, mood_happy_probability, mood_party_not_party, mood_party_party, mood_party_probability, mood_relaxed_not_relaxed, mood_relaxed_relaxed, mood_relaxed_probability, mood_sad_not_sad, mood_sad_sad, mood_sad_probability, moods_mirex_Cluster1, moods_mirex_Cluster2, moods_mirex_Cluster3, moods_mirex_Cluster4, moods_mirex_Cluster5, moods_mirex_probability, timbre_bright, timbre_dark, timbre_probability, tonal_atonal_atonal, tonal_atonal_tonal, tonal_atonal_probability, voice_instrumental_instrumental, voice_instrumental_voice, voice_instrumental_probability) values (?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement insertFormula = null;
        try {
            insertFormula = getInstance().getConnection().prepareStatement(sb.toString());
            for (int i = 0; i < formula.size(); i++) {
                insertFormula.setDouble(i + 1, formula.get(i));
            }
            insertFormula.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Vector<Double> getFormula() {
        Vector<Double> toReturn = new Vector<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM formula");
        PreparedStatement getFormula = null;
        try {
            getFormula = getInstance().getConnection().prepareStatement(sb.toString());
            ResultSet rs = getFormula.executeQuery();
            int nCol = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int i = 0; i < nCol; i++) {
                    toReturn.add(rs.getDouble(i + 1));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return toReturn;
    }

    public static boolean isFormulaExists() {
        StringBuilder sb = new StringBuilder("SELECT * FROM formula");
        PreparedStatement checkFormula = null;
        boolean toReturn = false;
        try {
            checkFormula = getInstance().getConnection().prepareStatement(sb.toString());
            ResultSet rs = checkFormula.executeQuery();
            if (rs.next() && rs.getDouble("danceable") != 0) {
                toReturn = true;
            } else {
                toReturn = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return toReturn;
    }

    public static Vector<Double> getScoreVector() {
        StringBuilder sb = new StringBuilder("SELECT compare_score FROM compare_database");
        PreparedStatement getMatrixStatment = null;
        Vector<Double> scoreVec = new Vector<>();
        try {
            getMatrixStatment = getInstance().getConnection().prepareStatement(sb.toString());
            ResultSet rs = getMatrixStatment.executeQuery();
            while (rs.next()) {
                scoreVec.add(rs.getDouble("compare_score"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return scoreVec;
    }
    
    public static boolean isSongExists(String i_Title, String i_Artist) {

        boolean isSongExists = false;
        String isSongExistsQuery = "select * from songs where title = ? and artist = ?";
        try {

            PreparedStatement isSongExistsStatement = getInstance().getConnection().prepareStatement(isSongExistsQuery);
            isSongExistsStatement.setString(1, i_Title);
            isSongExistsStatement.setString(2, i_Artist);

            ResultSet rSet = isSongExistsStatement.executeQuery();

            if (rSet.next()) {
                isSongExists = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isSongExists;
    }

    public static Matrix getCompareAlgMatrix() {
        StringBuilder sb = new StringBuilder("SELECT danceable, not_danceable, danceability_probability, female, male, gender_probability, genre_alternative, genre_blues, genre_electronic, genre_folkcountry, genre_funksoulrnb, genre_jazz, genre_pop, genre_raphiphop, genre_rock, genre_probability, electronic_ambient, electronic_dnb, electronic_house, electronic_techno, electronic_trance, electronic_probability, rosamerica_cla, rosamerica_dan, rosamerica_hip, rosamerica_jaz, rosamerica_pop, rosamerica_rhy, rosamerica_roc, rosamerica_spe, rosamerica_probablity, tzanetakis_blu, tzanetakis_cla, tzanetakis_cou, tzanetakis_dis, tzanetakis_hip, tzanetakis_jaz, tzanetakis_met, tzanetakis_pop, tzanetakis_reg, tzanetakis_roc, tzanetakis_proababilty, ismir04_rhythm_ChaChaCha, ismir04_rhythm_Jive, ismir04_rhythm_Quickstep, ismir04_rhythm_Rumba_American, ismir04_rhythm_Rumba_International, ismir04_rhythm_Rumba_Misc, ismir04_rhythm_Samba, ismir04_rhythm_Tango, ismir04_rhythm_VienneseWaltz, ismir04_rhythm_Waltz, ismir04_rhythm_probability, mood_acoustic_acoustic, mood_acoustic_not_acoustic, mood_acoustic_probability, mood_aggressive_aggressive, mood_aggressive_not_aggressive, mood_aggressive_probability, mood_electronic_electronic, mood_electronic_not_electronic, mood_electronic_probability, mood_happy_happy, mood_happy_not_happy, mood_happy_probability, mood_party_not_party, mood_party_party, mood_party_probability, mood_relaxed_not_relaxed, mood_relaxed_relaxed, mood_relaxed_probability, mood_sad_not_sad, mood_sad_sad, mood_sad_probability, moods_mirex_Cluster1, moods_mirex_Cluster2, moods_mirex_Cluster3, moods_mirex_Cluster4, moods_mirex_Cluster5, moods_mirex_probability, timbre_bright, timbre_dark, timbre_probability, tonal_atonal_atonal, tonal_atonal_tonal, tonal_atonal_probability, voice_instrumental_instrumental, voice_instrumental_voice, voice_instrumental_probability FROM compare_database");
        PreparedStatement getMatrixStatment = null;
        Matrix mat = null;
        try {
            getMatrixStatment = getInstance().getConnection().prepareStatement(sb.toString());
            ResultSet rs = getMatrixStatment.executeQuery();
            int nCol = rs.getMetaData().getColumnCount();
            List<double[]> table = new ArrayList<>();
            while (rs.next()) {
                double[] row = new double[nCol];
                for (int iCol = 1; iCol <= nCol; iCol++) {
                    row[iCol - 1] = rs.getDouble(iCol);
                }
                table.add(row);
            }
            double[][] matVals = new double[table.size()][nCol];
            for (int i = 0; i < table.size(); i++) {
                System.arraycopy(table.get(i), 0, matVals[i], 0, nCol);
            }
            mat = new Matrix(matVals);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mat;
    }
}
