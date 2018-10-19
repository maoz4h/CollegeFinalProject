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
import Objects.User;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import DB.DBControl;
import Objects.Song;
import java.util.ArrayList;
import java.util.Vector;


@WebServlet(urlPatterns = { "/ServletSignIn" })
public class ServletSignIn extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> m_DevelopersEmails = new ArrayList<String>(){{
        add("maoz4h@gmail.com");
        add("avivm74@gmail.com");
        add("a@a.com");
}};
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("i_Email");
        String password = request.getParameter("i_Password");
        String result = null;

        /*
		 * TODO:
		 * -implement a check if the strings are empty \ null \ don't exist in the database as a signed user
		 * -an optional sign up (in case the user didn't signed up)
         */
        if (!User.isSignInValid(email, password)) {
            result = "SignIn.html";
        } else {
            User currentUser = DBControl.getUserByEmail(email);
            
            HttpSession session = request.getSession();
            Gson gson = new Gson();
            session.setAttribute("userEmail", email);
            session.setAttribute("userInfo", gson.toJson(currentUser));
            result = "UserPage.html";
        }
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher(result);
        NextPageAccordingResult.forward(request, response);
    }
}
