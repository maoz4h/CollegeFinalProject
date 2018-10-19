/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

import Servlets.ServletScanPlaylist;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author Maoz
 */
public class Playlist {

    private File m_File;
    private String m_FolderPath;
    private String m_Name;

    //Getters
    public File getFile() {
        return m_File;
    }

    public String getFolderPath() {
        return m_FolderPath;
    }

    public String getName() {
        if (m_Name != null) {
            return m_Name;
        } else {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH_mm_ss");
            Date date = new Date();
            m_Name = "New Playlist " + dateFormat.format(date) + ".m3u";
            return m_Name;
        }
    }

    //Setters
    public void setFile(File m_File) {
        this.m_File = m_File;
    }

    public void setFolderPath(String m_FolderPath) {
        this.m_FolderPath = m_FolderPath;
    }

    public void setName(String m_Name) {
        this.m_Name = m_Name;
    }

    //C'tor
    public Playlist(String i_FolderPath) {
        m_FolderPath = i_FolderPath;
        getName();
    }

    public void BuildPlaylist(Vector<PreSong> i_PreSongs, Song i_RepSong, Double i_Sensitivity) {
        //create the new file
        try {
            StringBuilder sb = new StringBuilder();
            addHeaderToString(sb);
            //Add the song with the highest comparison score with the repSong.
            //then, add the song with the highest comparison score with the 1st added song,...

            Vector<Song> existingPlaylistSongs = getSongs(i_PreSongs);

            //if (existingPlaylistSongs.size() > 0) {
                Song bestMatchedSong = i_RepSong;
                int existingPlaylistSongsLength = existingPlaylistSongs.size();

                for (; existingPlaylistSongsLength > 0; existingPlaylistSongsLength--) {
                    bestMatchedSong = addNextBestMatch(bestMatchedSong, existingPlaylistSongs);
                    //add the bestMatch to the new playlist:
                    addSongToString(bestMatchedSong, bestMatchedSong.get_Runtime(), bestMatchedSong.get_Path(), sb);
                    existingPlaylistSongs.remove(bestMatchedSong);
                }
                getName();
                m_File = new File(m_Name);
                m_File.createNewFile();
                addStringToFile(sb);
            //}
            //else
            //{
                //throw new Exception("There are no songs files with meta data on them.");
            //}
        }
        catch (IOException ex)
        {
            Logger.getLogger(Playlist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addSongToString(Song i_TempSong, int i_Runtime, String i_Path, StringBuilder i_sb) {
        addFileInfoToString(i_sb);
        addRuntimeToString(i_Runtime, i_sb);
        addTitleArtistToString(i_TempSong, i_sb);
        addPathToString(i_Path, i_sb);
    }

    private void addHeaderToString(StringBuilder i_sb) {
        i_sb.append("#EXTM3U" + System.getProperty("line.separator"));
    }

    private void addFileInfoToString(StringBuilder i_sb) {
        i_sb.append("#EXTINF:");
    }

    private void addRuntimeToString(int i_Runtime, StringBuilder i_sb) {
        i_sb.append(i_Runtime);
    }

    private void addTitleArtistToString(Song i_TempSong, StringBuilder i_sb) {
        i_sb.append("," + i_TempSong.getTitle() + " - " + i_TempSong.getArtist() + System.getProperty("line.separator"));
    }

    private void addStringToFile(StringBuilder sb) {
        FileWriter fw;
        try {
            fw = new FileWriter(m_File.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();

        } catch (IOException ex) {
            Logger.getLogger(Playlist.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addPathToString(String i_Path, StringBuilder i_sb) {
        i_sb.append(i_Path + System.getProperty("line.separator"));
    }

    private Vector<Song> getSongs(Vector<PreSong> i_PreSongs) {
        Vector<Song> songs = new Vector<Song>();

        for (PreSong currentPreSong : i_PreSongs) {
            String title = currentPreSong.get_Title();
            String artist = currentPreSong.get_Artist();
            String path = currentPreSong.get_Path();
            int runtime = currentPreSong.getRuntime();  //default runtime = -1
            if (title != null && artist != null && path != null && runtime != -1) {
                Song newSong = new Song(title, artist, path, runtime, false);
                if (newSong.getData().isHasData())
                {
                    songs.add(newSong);
                }
            }
        }
        return songs;
    }

    private Song addNextBestMatch(Song bestMatchedSong, Vector<Song> i_Songs) {
        Comparison bestMatch = null;
        Song bestSong = null;
        for (Song currentSong : i_Songs) {
            Comparison comparison = new Comparison(bestMatchedSong, currentSong);
            if (bestMatch == null) {
                bestMatch = comparison;
                bestSong = currentSong;
            } else if (bestMatch.getM_ComparisonScore() < comparison.getM_ComparisonScore()) {
                bestMatch = comparison;
                bestSong = currentSong;
            }
        }
        return bestSong;
    }
}
