package Objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Part;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PrePlaylist {

    private Vector<PreSong> m_PrePlaylist;
    private int next;

    public Vector<PreSong> getPrePlaylist() {
        return m_PrePlaylist;
    }

    public Vector<PreSong> GetPrePlaylistByM3U(File f) {
        m_PrePlaylist = new Vector<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.getPath()), "UTF-8"))) {
            String line1, line2;
            line1 = br.readLine();
            while ((line1 = br.readLine()) != null) {
                line2 = br.readLine();
                getTitleArtistPathRuntime(line1, line2);
            }
        } catch (IOException ex) {
            System.out.println("error reading the file");
        }
        next = -1;
        return m_PrePlaylist;
    }

    public void PrePlayListByFolder(String i_path) {
        try {
            m_PrePlaylist = new Vector<>();
            File folder = new File(i_path);
            File[] listOfMp3Files = folder.listFiles();
            for (File file : listOfMp3Files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    InputStream input = new FileInputStream(new File(file.getPath()));
                    DefaultHandler handler = new DefaultHandler();
                    Metadata metadata = new Metadata();
                    Parser parser = new Mp3Parser();
                    ParseContext parseCtx = new ParseContext();
                    parser.parse(input, handler, metadata, parseCtx);
                    input.close();
                    
                    String title = metadata.get("title");
                    if (title == null)
                    {
                        title = file.getName();
                        title = title.substring(0, title.length() - 4);
                    }
                    
                    String artist = metadata.get("xmpDM:artist");
                    String runtimeStr = metadata.get("xmpDM:duration");
                    Double runtimeDouble = Double.parseDouble((runtimeStr));
                    int runtime = runtimeDouble.intValue() / 1000;

                    if (title != null && artist != null)
                    {
                        PreSong prsng = new PreSong(title, artist, i_path, runtime);
                        m_PrePlaylist.add(prsng);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
    }

    private void getTitleArtistPathRuntime(String line1, String line2) {

        if (line1 == null || line1.isEmpty() || line2 == null || line2.isEmpty()) {
            return;
        }

        int titleArtistSeperatorIndex = IndexOfSeperator(line1, '-');
        String title = null;
        String artist = null;
        String path = null;

        int runtime = Integer.parseInt(line1.substring(line1.indexOf(":") + 1, line1.indexOf(",")));

        title = line1.substring(line1.indexOf(",") + 1, titleArtistSeperatorIndex - 1);
        artist = line1.substring(titleArtistSeperatorIndex + 2);

        path = line2;
        PreSong prsng = new PreSong(title, artist, path, runtime);
        m_PrePlaylist.add(prsng);

    }

    public void PrePlaylistByM3U(Part filePart) {
        InputStream in = null;
        try {
            m_PrePlaylist = new Vector<>();
            in = filePart.getInputStream();
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String read;

            String line1, line2;
            line1 = br.readLine();

            while ((line1 = br.readLine()) != null) {
                line2 = br.readLine();
                getTitleArtistPathRuntime(line1, line2);
            }

            next = -1;
            br.close();

        } catch (IOException ex) {
            Logger.getLogger(PrePlaylist.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(PrePlaylist.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public int countStringInString(String i_TheString, String i_SubString) {

        int subStringCount = 0;

        String array[] = i_TheString.split(i_SubString);
        subStringCount = array.length - 1;

        return subStringCount;
    }

    private int IndexOfSeperator(String i_TheString, char i_char) {
        for (int i = i_TheString.indexOf("-"); i < i_TheString.length(); i++) {
            if (i_TheString.charAt(i) == i_char
                    && i_TheString.charAt(i + 1) == ' '
                    && i_TheString.charAt(i - 1) == ' ') {
                return i;
            }
        }
        return -1;
    }
}
