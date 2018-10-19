/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import Objects.InfoBoxForHtml;
import Objects.Playlist;
import Objects.PrePlaylist;
import Objects.Song;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.Collection;
import javax.servlet.RequestDispatcher;

/**
 *
 * @author Maoz
 */
@WebServlet(name = "ServletBuildPlaylistScanFolder", urlPatterns = {"/ServletBuildPlaylistScanFolder"})
@MultipartConfig
public class ServletBuildPlaylistScanFolder extends HttpServlet {

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
        String folderPath = null;
        //Double sensitivity = null;
        Collection<Part> files = null;

//get Inputs:
        try {
            folderPath = request.getParameter("i_FolderPath");
            files = request.getParts();
            songRepTitle = request.getParameter("i_SongRepTitle");
            songRepArtist = request.getParameter("i_SongRepArtist");
            //sensitivity = Double.parseDouble(request.getParameter("i_Sensitivity")) / 100;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//Build a new playlist, sorted with the files in the folder
        //String folderPath = getServletContext().getRealPath("");
        Playlist newPlaylist = new Playlist(folderPath);
        Song repSong = new Song(songRepTitle, songRepArtist, false);
        
        if (repSong.getData().isHasData() == true)
        {
            PrePlaylist prePlaylist = new PrePlaylist();
            prePlaylist.PrePlayListByFolder(folderPath);

            newPlaylist.BuildPlaylist(prePlaylist.getPrePlaylist(), repSong, null);
            File newPlaylistFile = newPlaylist.getFile();
            newPlaylist.getName();
            // You must tell the browser the file type you are going to send
            // for example application/pdf, text/plain, text/html, image/jpg
            response.setContentType(".m3u");

            // Make sure to show the download dialog
            response.setHeader("Content-disposition", "attachment; filename=" + newPlaylistFile.getName());

            // Assume file name is retrieved from database
            // For example D:\\file\\test.pdf
            // This should send the file to browser
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(newPlaylistFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.flush();
        }
        else
        {
            InfoBoxForHtml.showInfoBox("Representive song not found in Database.\n please try again with a different song","Failure, try to pick a different song");
        }
        InfoBoxForHtml.showInfoBox("Playlist downloaded, enter it to any player that plays .m3u playlists.", "Success PlayList Downloaded");
//        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html#&ui-state=dialog");
//        NextPageAccordingResult.forward(request, response);
    }
    
    

}
