/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import BaseAlgorithim.BaseInterface;
import Objects.Comparison;
import Objects.Song;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Maoz
 */
@WebServlet(name = "ServletDevelopers", urlPatterns = {"/ServletDevelopers"})
public class ServletDevelopers extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
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
        
        String songAtitle = null;
        String songAartist = null;
        String songBtitle = null;
        String songBartist = null;
        String score = null;
        
        Song songA = null;
        Song songB = null;
        
        //get inputs:
        try {
            songAtitle = request.getParameter("i_songAtitle");
            songAartist = request.getParameter("i_songAartist");
            songBtitle = request.getParameter("i_songBtitle");
            songBartist = request.getParameter("i_songBartist");
            score = request.getParameter("i_score");
            songA = new Song(songAtitle, songAartist, false);
            songB = new Song(songBtitle, songBartist, false);
            
        } catch (Exception ex) {
            //input exception
            Logger.getLogger(ServletScanPlaylist.class.getName()).log(Level.SEVERE, null, ex);
        }
        int num = Integer.parseInt(score);
        //here you can gat to the meta data. example:
        BaseInterface bi = new BaseInterface();
        bi.enterSongsToComparisionDB(songA, songB,(double)num);
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("Developers.html");
        NextPageAccordingResult.forward(request, response);
        
    }
}
