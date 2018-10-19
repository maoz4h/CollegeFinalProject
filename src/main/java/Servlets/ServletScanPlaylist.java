/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package Servlets;

// Import required java libraries
import Objects.Comparison;
import Objects.InfoBoxForHtml;
import Objects.Playlist;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.*;

import Objects.PrePlaylist;
import Objects.PreSong;
import Objects.Song;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Part;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import javax.servlet.RequestDispatcher;

/**
 *
 * @author Maoz
 */
@WebServlet(name = "ServletScanPlaylist", urlPatterns = {"/ServletScanPlaylist"})
@MultipartConfig
public class ServletScanPlaylist extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        
        String songRepTitle = null;
        String songRepArtist = null;        //Double sensitivity = null;
        Part filePart = null;
        
//get inputs:
        try {
            songRepTitle = request.getParameter("i_SongRepTitle");
            songRepArtist = request.getParameter("i_SongRepArtist");
            filePart = request.getPart("i_File"); // Retrieves <input type="file" name="i_File">
        } catch (ServletException ex) {
            //input exception
            Logger.getLogger(ServletScanPlaylist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//build a new sorted playlist from the uploaded playlist

        String folderPath = getServletContext().getRealPath("");
        Playlist newPlaylist = new Playlist(folderPath);
        Song repSong = new Song(songRepTitle, songRepArtist, false);
        
        PrePlaylist prePlaylist = new PrePlaylist();
        prePlaylist.PrePlaylistByM3U(filePart);
        
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
        InfoBoxForHtml.showInfoBox("Success, Your Playlist had been edited by our algorithim. Enjoy!", "Local Playlist Edited");
        
     //   RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html");
	//NextPageAccordingResult.forward(request, response);
    }

    private void downloadNewPlaylist(ServletContext context, String folderPath, File newPlaylistFile, HttpServletResponse response) throws IOException {
        // gets MIME type of the file
        String mimeType = context.getMimeType(folderPath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);

        // modifies response
        response.setContentType(mimeType);
        response.setContentLength((int) newPlaylistFile.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", newPlaylistFile.getName());
        response.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        try {
            FileInputStream inStream = new FileInputStream(newPlaylistFile);
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
                inStream.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServletScanPlaylist.class.getName()).log(Level.SEVERE, null, ex);
        }

        outStream.close();

    }
}
