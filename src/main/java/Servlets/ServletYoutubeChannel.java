/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

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
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
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
 * @author aviv
 */
@WebServlet(name = "ServletYoutubeChannel", urlPatterns = {"/ServletYoutubeChannel"})
public class ServletYoutubeChannel extends HttpServlet {

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
        
        String ChannelID= null;  
        StringBuilder channelUrlAdress = new StringBuilder("https://www.youtube.com/channel/");
        try {
            ChannelID= getChannelID();
            if (ChannelID!=null)
            {
                channelUrlAdress.append(ChannelID);
                channelUrlAdress.append("/playlists");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PrintWriter pw = response.getWriter();
        pw.print(channelUrlAdress.toString());
        pw.close();
       
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html");
	NextPageAccordingResult.forward(request, response);
        
        
    }
    private static YouTube youtube;
    
    public String getChannelID() {

        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
        String channelId= null;

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "channelbulletin");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-channelbulletin-sample").build();

            // Construct a request to retrieve the current user's channel ID.
            // See https://developers.google.com/youtube/v3/docs/channels/list
            YouTube.Channels.List channelRequest = youtube.channels().list("id,snippet");
            channelRequest.setMine(true);
            

            // In the API response, only include channel information needed
            // for this use case.
            channelRequest.setFields("items(id,snippet/title)");
            ChannelListResponse channelResult = channelRequest.execute();

            List<Channel> channelsList = channelResult.getItems();

            if (channelsList != null) {
                // The user's default channel is the first item in the list.
                 channelId = channelsList.get(0).getId();
                
                return channelId; 
            } 
            
        }
        catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return  channelId;
    }

}
