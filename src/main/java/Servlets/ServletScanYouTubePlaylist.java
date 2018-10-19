/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Objects.Comparison;
import Objects.Song;
import YouTubeApi.Auth;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import javax.servlet.RequestDispatcher;
import Objects.InfoBoxForHtml;
/**
 *
 * @author aviv
 */
@WebServlet(name = "ServletScanYouTubePlaylist", urlPatterns = {"/ServletScanYouTubePlaylist"})
public class ServletScanYouTubePlaylist extends HttpServlet {

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
        String songRepTitle = null;
        String songRepArtist = null;
        String YoutubeUrl = null;
        int songsAmountLimit = 0;

        //get Inputs:
        try {
            songsAmountLimit = Integer.parseInt(request.getParameter("i_SongsAmountLimit"));
            if (songsAmountLimit < 10) {
                throw new Exception("Songs Amount Limit is smaller than 10.");
            }
            YoutubeUrl = request.getParameter("i_YoutubePlaylist");
            songRepTitle = request.getParameter("i_SongRepTitle");
            songRepArtist = request.getParameter("i_SongRepArtist");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Song repSong = new Song(songRepTitle, songRepArtist, false);
        Vector<Song> SongsVectorFromYoutube = createPlaylistByYouTube(YoutubeUrl);
        Vector<Song> sortedSongsFromYoutube = getAllSongsSortedByRepSong(repSong, SongsVectorFromYoutube, songsAmountLimit);
        createYouTubePlaylist(sortedSongsFromYoutube);
        InfoBoxForHtml.showInfoBox("Success, a new Youtube playlist has been added to your Youtube channel. Have fun.", "Scan Youtube Playlist Success");
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html");
	NextPageAccordingResult.forward(request, response);

    }

    private Vector<Song> getAllSongsSortedByRepSong(Song repSong, Vector<Song> i_SongsVectorFromYoutubeint, int i_SongsAmountLimit) {

        Vector<Song> sortedSongsFromYoutube = new Vector<Song>();

        if (repSong == null || !repSong.getData().isHasData()) {
            try {
                throw new Exception("Rep Song has not been found.");
            } catch (Exception ex) {
                Logger.getLogger(ServletSetYouTubePlaylistByDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (i_SongsAmountLimit > 0) {
            Song bestMatchedSong = repSong;
            int existingPlaylistSongsLength = i_SongsVectorFromYoutubeint.size();

            for (; i_SongsAmountLimit > 0 && existingPlaylistSongsLength > 0; i_SongsAmountLimit--, existingPlaylistSongsLength--) {
                bestMatchedSong = addNextBestMatch(bestMatchedSong, i_SongsVectorFromYoutubeint);
                //add the bestMatch to the new playlist:
                sortedSongsFromYoutube.add(bestMatchedSong);
                i_SongsVectorFromYoutubeint.remove(bestMatchedSong);
            }
        }
        return sortedSongsFromYoutube;
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

    private static YouTube youtube;
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    public static Vector<Song> createPlaylistByYouTube(String i_YoutubeUrl) throws IOException {

        // This OAuth 2.0 access scope allows for read-only access to the
        // authenticated user's account, but not other types of account access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.readonly");
        Vector<Song> SongsVectorFromYoutube = null;

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "myuploads");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-myuploads-sample").build();

            YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
            channelRequest.setMine(true);
            channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();

            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {

                // Define a list to store items in the list of uploaded videos.
                List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

                // Retrieve the playlist of the channel's uploaded videos.
                YouTube.PlaylistItems.List playlistItemRequest
                        = youtube.playlistItems().list("id,contentDetails,snippet");

                // another example link https://www.youtube.com/watch?v=iX-QaNzd-0Y&list=RDV1Al3wMiEek&index=3
                String playpistId = extractYTId(i_YoutubeUrl);    // https://www.youtube.com/watch?v=Z-PS7jmSiMY&list=RDZ-PS7jmSiMY
                playlistItemRequest.setPlaylistId(playpistId);    // PLPTp0D0svcF9a1aiH1T7xx-mxSztJs3Ed

                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

                String nextToken = "";

                // Call the API one or more times to retrieve all items in the
                // list. As long as the API response returns a nextPageToken,
                // there are still more items to retrieve.
                while (nextToken != null) {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                };

                // Prints information about the results.
                SongsVectorFromYoutube = ConvertPlaylistToSongsVector(playlistItemList.size(), playlistItemList.iterator());
            }

        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return SongsVectorFromYoutube;
    }

    /*
     * Print information about all of the items in the playlist.
     *
     * @param size size of list
     *
     * @param iterator of Playlist Items from uploaded Playlist
     */
    private static String extractYTId(String youTubeUrl) {

        //  String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        //  String pattern = "(?:youtube\\.com.*(?:\\?|&)(?:list)=)((?!videoseries)[a-zA-Z0-9_]*)";
        String pattern = "(?:(?:\\?|&)list=)((?!videoseries)[-a-zA-Z0-9_]*)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            String extractTheIdWithList = matcher.group().toString();
            String playlistid = extractTheIdWithList.substring(extractTheIdWithList.indexOf("list") + 5);

            return playlistid;
        } else {
            return "error";
        }
    }

    private static Vector<Song> ConvertPlaylistToSongsVector(int size, Iterator<PlaylistItem> playlistEntries) {

        int i = 1;
        String fullNameSong = null;
        String artist = null;
        String title = null;
        Song newSong = null;
        Vector<Song> newSongsVector = new Vector<>();

        while (playlistEntries.hasNext()) {

            PlaylistItem playlistItem = playlistEntries.next();
            fullNameSong = playlistItem.getSnippet().getTitle();
            if (fullNameSong.contains("-")) {
                artist = fullNameSong.substring(0, fullNameSong.indexOf("-"));
                title = fullNameSong.substring(fullNameSong.indexOf("-") + 2);
                //       System.out.println(i+". video name  = " + artist + " - " +title );  // print for test

                newSong = new Song(title, artist, false);
                if (!newSong.getData().isHasData()) {
                    //if there is no data -> switch between the strings:
                    newSong = new Song(artist, title, false);
                }
                if (newSong.getData().isHasData()) {
                    // if there is data -> ass this newSong to the vector
                    newSongsVector.add(newSong);
                }
                i++;
            }
        }

        return newSongsVector;
    }

    private static void getFirstString(String i_videosStrings) {

    }

    private static void getSecondString(String i_videosStrings) {

    }

    private void createYouTubePlaylist(Vector<Song> sortedSongsFromDB) {   //duble code from here

        ArrayList<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "playlistupdates");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Create a new, private playlist in the authorized user's channel.
            String playlistId = insertPlaylist();

            // If a valid playlist was created, add a video to that playlist.
            ArrayList<String> VideosToInsert = getStringsVectorFromSongsVector(sortedSongsFromDB);

            for (String video : VideosToInsert) {

                insertPlaylistItem(playlistId, video);
            }

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
    }

    private static String insertPlaylist() throws IOException {

        // This code constructs the playlist resource that is being inserted.
        // It defines the playlist's title, description, and privacy status.
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle("Playlist_Factory " + LocalDateTime.now());
        playlistSnippet.setDescription("A private playlist created with the YouTube API v3");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("public");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        // Call the API to insert the new playlist. In the API call, the first
        // argument identifies the resource parts that the API response should
        // contain, and the second argument is the playlist being inserted.
        YouTube.Playlists.Insert playlistInsertCommand
                = youtube.playlists().insert("snippet,status", youTubePlaylist);
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
        for (Song song : sortedSongsFromDB) {
            //       result.add(song.getArtist() + " " + song.getTitle());  
            String songByString = song.getArtist() + " " + song.getTitle();
            try {
                result.add(chooseVideos(songByString));
            } catch (IOException ex) {
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
        String str = rId.getVideoId().toString();
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
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand
                = youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
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
