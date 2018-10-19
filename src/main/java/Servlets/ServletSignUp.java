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


@WebServlet(urlPatterns = { "/ServletSignUp" })
public class ServletSignUp extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("i_Email");
        String password = request.getParameter("i_Password");
        String confirmPassword = request.getParameter("i_ConfirmPassword");
        String name = request.getParameter("i_Name");
        String result = null;

        /*
		 * TODO:
		 * -implement a check if the strings are empty \ null \ don't exist in the database as a signed user
		 * -an optional sign up (in case the user didn't signed up)
         */
        if (!User.isSignUpValid(email, password, confirmPassword, name)) {
            result = "SignUp.html";
        } else {
            User currentUser = DBControl.setNewUser(email, password, name);
            
            HttpSession session = request.getSession();
            session.setAttribute("userEmail", email);
            result = "UserPage.html";
        }
        RequestDispatcher NextPageAccordingResult = request.getRequestDispatcher(result);
        NextPageAccordingResult.forward(request, response);
    }
}


