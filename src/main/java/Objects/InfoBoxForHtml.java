/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author אסף
 */
public class InfoBoxForHtml {
    public static void showInfoBox(String infoMessage, String titleBar)
    {
       
        UIManager UI=new UIManager();
       UI.put("OptionPane.background", Color.white);
        UI.put("Panel.background", Color.white);
       JOptionPane.showMessageDialog(null,infoMessage,titleBar, JOptionPane.INFORMATION_MESSAGE);  
                  
   
    }
}
