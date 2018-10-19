package Objects;

public class PreSong {

    private String m_Title;
    private String m_Artist;
    private String m_Path;
    private int m_Runtime;

    public int getRuntime() {
        return m_Runtime;
    }
    
    public void setRuntime(int i_Runtime) {
        this.m_Runtime = i_Runtime;
    }

    public String get_Title() {
        return m_Title;
    }

    public String get_Artist() {
        return m_Artist;
    }

    public String get_Path() {
        return m_Path;
    }

    public PreSong() {

    }

    public PreSong(String i_Title, String i_Artist, String i_Path, int i_Runtime) {
        m_Title = i_Title;
        m_Artist = i_Artist;
        m_Path = i_Path;
        m_Runtime = i_Runtime;
    }

}
