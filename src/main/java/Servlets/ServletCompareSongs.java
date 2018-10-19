/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

/**
 *
 * @author Maoz
 */
import java.io.IOException;
import java.io.PrintWriter;
import Objects.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import DB.*;
import Objects.Song;
import Objects.DataFetcher;
import com.google.gson.JsonArray;
import java.util.Vector;
import java.util.logging.*;


@WebServlet(urlPatterns = { "/ServletCompareSongs" })
public class ServletCompareSongs extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {        
        request.setCharacterEncoding("UTF-8");
        String songAtitle = request.getParameter("i_SongAtitle");
        String songAartist = request.getParameter("i_SongAartist");
        String songBtitle = request.getParameter("i_SongBtitle");
        String songBartist = request.getParameter("i_SongBartist");
        
        Song songA = new Song(songAtitle, songAartist, false);
        Song songB = new Song(songBtitle, songBartist, false);
        
        Comparison comparison = new Comparison(songA, songB);
        
        Gson gson = new Gson();
	String score = gson.toJson(comparison.getM_ComparisonScore());
	PrintWriter pw = response.getWriter();
	pw.print(score);
	pw.close();
                
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher("UserPage.html#&ui-state=dialog");
	NextPageAccordingResult.forward(request, response);
    }

    private void compareWithTheRestComparisons(Comparison i_Comparison) {
        DBControl dbControl = DBControl.getInstance();
        Vector<Comparison> comparisons = dbControl.getComparisons();
        for (Comparison comparison : comparisons)
        {
            Song i_SongA = new Song(i_Comparison.getM_SongAtitle(), i_Comparison.getM_SongAartist(), false);
            Song i_SongB = new Song(i_Comparison.getM_SongBtitle(), i_Comparison.getM_SongBartist(), false);
            Song songA = new Song(comparison.getM_SongAtitle(), comparison.getM_SongAartist(), false);
            Song songB = new Song(comparison.getM_SongBtitle(), comparison.getM_SongBartist(), false);
            
            try{
                Comparison a = new Comparison(i_SongA, songA);
                Comparison b = new Comparison(i_SongA, songB);
                Comparison c = new Comparison(i_SongB, songA);
                Comparison d = new Comparison(i_SongB, songB);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}


