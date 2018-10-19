/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Objects.InfoBoxForHtml;
import Objects.Comparison;
import Objects.Song;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import YouTubeApi.Auth;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import javax.servlet.RequestDispatcher;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkListener;
import sun.java2d.loops.ProcessPath.ProcessHandler;
/**
 *
 * @author Maoz
 */
@WebServlet(name = "ServletSetYouTubePlaylistByDB", urlPatterns = {"/ServletSetYouTubePlaylistByDB"})
public class ServletSetYouTubePlaylistByDB extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        int songsAmountLimit = 0;
        String songRepTitle = null;
        String songRepArtist = null;
        String playlistUrlAdress= null;

        //get Inputs:
        try {
            songsAmountLimit = Integer.parseInt(request.getParameter("i_SongsAmountLimit"));
            if (songsAmountLimit < 10) {
                throw new Exception("Songs Amount Limit is smaller than 10.");
            }
            songRepTitle = request.getParameter("i_SongRepTitle");
            songRepArtist = request.getParameter("i_SongRepArtist");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //get the repSong as an object
        Song repSong = new Song(songRepTitle, songRepArtist, false);
        //get all the songs from the DB, sorted by the highest comparison score to the lowest (by the repSong)
        Vector<Song> sortedSongsFromDB = getAllSongsSortedByRepSong(repSong, songsAmountLimit);
        //TODO: call youtube api and create from sortedSongsFromDB a new playlist
        sortedSongsFromDB.add(0,repSong);
        playlistUrlAdress= createYouTubePlaylist(sortedSongsFromDB);
        InfoBoxForHtml.showInfoBox("Success, your new Youtube playlist has been added to your Youtube channel. Have fun. ", "Database Scanned");     
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html");
	NextPageAccordingResult.forward(request, response);
        
        
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

    private Vector<Song> getAllSongsSortedByRepSong(Song repSong, int i_SongsAmountLimit) {
        Vector<Song> sortedSongsFromDB = new Vector<Song>();

        if (repSong == null || !repSong.getData().isHasData()) {
            try {
                throw new Exception("Rep Song has not been found.");
            } catch (Exception ex) {
                Logger.getLogger(ServletSetYouTubePlaylistByDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Vector<Song> allSongsFromDB = DB.DBControl.getInstance().getAllSongs();

        if (i_SongsAmountLimit > 0) {
            Song bestMatchedSong = repSong;
            int existingPlaylistSongsLength = allSongsFromDB.size();

            for (; existingPlaylistSongsLength > 0 && i_SongsAmountLimit > 0; existingPlaylistSongsLength--, i_SongsAmountLimit--) {
                bestMatchedSong = addNextBestMatch(bestMatchedSong, allSongsFromDB);
                //add the bestMatch to the new playlist:
                sortedSongsFromDB.add(bestMatchedSong);
                allSongsFromDB.remove(bestMatchedSong);
            }
        }
        return sortedSongsFromDB;
    }

    private static YouTube youtube;
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    
    private String createYouTubePlaylist(Vector<Song> sortedSongsFromDB) {
        ArrayList<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
        StringBuilder playlistUrlAdress = new StringBuilder("https://www.youtube.com/playlist?list=");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "playlistupdates");
            
        
            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Create a new, private playlist in the authorized user's channel.
            String playlistId = insertPlaylist();
            playlistUrlAdress.append(playlistId);

            // If a valid playlist was created, add a video to that playlist.
            
            ArrayList<String> VideosToInsert = getStringsVectorFromSongsVector(sortedSongsFromDB);
            
            for (String video : VideosToInsert) {
            	
            	insertPlaylistItem(playlistId, video);
            }

            int i=0;
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
        return playlistUrlAdress.toString();
    }
    
    private static String insertPlaylist() throws IOException {

        // This code constructs the playlist resource that is being inserted.
        // It defines the playlist's title, description, and privacy status.
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle("Playlist_Factory "  + LocalDateTime.now());
        playlistSnippet.setDescription("A private playlist created with the YouTube API v3");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("public");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        // Call the API to insert the new playlist. In the API call, the first
        // argument identifies the resource parts that the API response should
        // contain, and the second argument is the playlist being inserted.
        YouTube.Playlists.Insert playlistInsertCommand =
                youtube.playlists().insert("snippet,status", youTubePlaylist);
        Playlist playlistInserted = playlistInsertCommand.execute();

        // Print data from the API response and return the new playlist's
        // unique playlist ID.
        System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
        System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
        System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
        System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
        return playlistInserted.getId();

    }

    private ArrayList<String> getStringsVectorFromSongsVector(Vector<Song> sortedSongsFromDB) {
        ArrayList<String> result = new ArrayList<String>();
        for(Song song : sortedSongsFromDB){
     //       result.add(song.getArtist() + " " + song.getTitle());  
            String songByString= song.getArtist() + " " + song.getTitle();
            try {
                result.add(chooseVideos(songByString));
            } 
            catch (IOException ex) {
                Logger.getLogger(ServletSetYouTubePlaylistByDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
        return result;
    }
    
    private static String chooseVideos(String i_SongByString) throws IOException {
    	
    	Properties properties = new Properties();
        try {
            InputStream in = YouTubeApi.Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
 //       String queryTerm = YouTubeApi.Search.getInputQuery();
        String queryTerm = i_SongByString;

        // Define the API request for retrieving search results.
        YouTube.Search.List search = youtube.search().list("id,snippet");
        

        // Set your developer key from the {{ Google Cloud Console }} for
        // non-authenticated requests. See:
        // {{ https://cloud.google.com/console }}
        String apiKey = properties.getProperty("youtube.apikey");
        search.setKey(apiKey);
        search.setQ(queryTerm);

        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("video");

        // To increase efficiency, only retrieve the fields that the
        // application uses.
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

        // Call the API and print results.
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();
     	 SearchResult singleVideo = searchResultList.get(0);
        ResourceId rId = singleVideo.getId();
        String str= rId.getVideoId().toString();	
    	return str;
    }
    
    private static String insertPlaylistItem(String playlistId, String videoId) throws IOException {

        // Define a resourceId that identifies the video being added to the
        // playlist.
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        // Set fields included in the playlistItem resource's "snippet" part.
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        // Create the playlistItem resource and set its snippet to the
        // object created above.
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        // Call the API to add the playlist item to the specified playlist.
        // In the API call, the first argument identifies the resource parts
        // that the API response should contain, and the second argument is
        // the playlist item being inserted.
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

        // Print data from the API response and return the new playlist
        // item's unique playlistItem ID.

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        return returnedPlaylistItem.getId();

    }
}
