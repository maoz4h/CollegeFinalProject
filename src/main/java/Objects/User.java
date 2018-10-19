/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

import DB.DBControl;
import java.io.Serializable;

/**
 *
 * @author Maoz
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
 
	private String m_Email;
	
	//User details
	private String m_Password;
	private String m_Name;
	
	//C'tors
	public User(){}
	
	public User(String i_Email, String i_Password, String i_Name)
	{
		//TODO: check it i_Email is not already exists in the DB,
		//if it isn't -> continue 
		m_Email = i_Email;
		m_Password = i_Password;
		m_Name = i_Name;
	}
	
	//Getters
	public String getM_Email() {
		return m_Email;
	}
	
	public String getM_Name() {
		return m_Name;
	}
	
	public String getM_Password() {
		return m_Password;
	}
	
	//Setters
	public void setM_Password(String i_Password) {
		this.m_Password = i_Password;
	}
	public void setM_Email(String i_Email) {
		this.m_Email = i_Email;
	}
	public void setM_Name(String i_Name) {
		this.m_Name = i_Name;
	}
        
	//Valid Checks
    private static boolean isNameValid(String i_Name) {
        boolean checkResult = true;
        if (i_Name == null || i_Name == "") {
            checkResult = false;
        }
        return checkResult;
    }

    private static boolean isPasswordValid(String i_Password, String i_ConfirmPassword) {
        boolean checkResult = true;
        if (i_Password == null || i_Password.length() < 5 || !i_Password.equals(i_ConfirmPassword)) {
            checkResult = false;
        }
        return checkResult;
    }

    private static boolean isEmailValid(String i_Email) {
        boolean checkResult = true;
        if (i_Email == null || i_Email.length() < 5 || !i_Email.contains("@") || isUserExists(i_Email)) {
            checkResult = false;
        }
        return checkResult;
    }

    public static boolean isUserExists(String i_Email){
        boolean checkResult = false;
        DBControl dbControl = DBControl.getInstance();
        if (dbControl.isUserExistsByEmail(i_Email)){
            checkResult = true;
        }
        return checkResult;
    }
    
    public static boolean isSignUpValid(String i_Email, String i_Password,String i_ConirmPassword, String i_Name) {
        boolean checkResult = true;
        if (!isNameValid(i_Name) || !isPasswordValid(i_Password, i_ConirmPassword) || !isEmailValid(i_Email)) {
            checkResult = false;
        }
        return checkResult;
    }

    public static boolean isSignInValid(String i_Email, String i_Password) {
        boolean checkResult = false;
        DBControl dbControl = DBControl.getInstance();
        if (dbControl.isUserExistsByEmail(i_Email)) {
            if (dbControl.GetUserPasswordByEmail(i_Email).equals(i_Password)) {
                checkResult = true;
            }
        }
        return checkResult;
    }
}