package pbo2;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class Tugas6 extends JFrame {

    // ===== UI components =====
    private final JTextField txtCity = new JTextField();
    private final JComboBox<String> cbFavorites = new JComboBox<>();
    private final JButton btnCheck = new JButton("Cek Cuaca");
    private final JButton btnAddFav = new JButton("Tambah Favorit");
    private final JButton btnSaveCsv = new JButton("Simpan CSV");
    private final JButton btnLoadCsv = new JButton("Muat CSV");

    private final JLabel lblLocation = new JLabel("-");
    private final JLabel lblTemp = new JLabel("-");
    private final JLabel lblHumidity = new JLabel("-");
    private final JLabel lblWind = new JLabel("-");
    private final JLabel lblCondition = new JLabel("-");
    private final JLabel lblUpdated = new JLabel("-");
    private final JLabel lblIcon = new JLabel();

    private final JTable table;
    private final DefaultTableModel tableModel;

    // ===== Helpers =====
    private final Highlighter.HighlightPainter painter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255,255,0,160));

    private String apiKey;
    private static final File API_KEY_FILE = new File("owm_api_key.txt");
    private static final File FAV_FILE = new File("favorites.txt");

    // ===== Constructor =====
    public Tugas6() {
        super("Aplikasi Cek Cuaca (OpenWeatherMap)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12,12));

        // Top: input & favorites
        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setBorder(new EmptyBorder(8,8,0,8));

        JPanel input = new JPanel(new GridLayout(2,1,8,6));
        JPanel row1 = new JPanel(new BorderLayout(8,6));
        row1.add(new JLabel("Kota:"), BorderLayout.WEST);
        txtCity.setToolTipText("Contoh: Banjarmasin, Jakarta, London");
        row1.add(txtCity, BorderLayout.CENTER);
        JPanel row1Right = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        row1Right.add(btnCheck);
        row1Right.add(btnAddFav);
        row1.add(row1Right, BorderLayout.EAST);

        JPanel row2 = new JPanel(new BorderLayout(8,6));
        row2.add(new JLabel("Favorit:"), BorderLayout.WEST);
        cbFavorites.setEditable(false);
        cbFavorites.setToolTipText("Pilih kota favorit untuk cepat mengecek cuaca");
        row2.add(cbFavorites, BorderLayout.CENTER);
        JPanel row2Right = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        row2Right.add(btnSaveCsv);
        row2Right.add(btnLoadCsv);
        row2.add(row2Right, BorderLayout.EAST);

        input.add(row1);
        input.add(row2);
        top.add(input, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Center: detail cuaca + ikon
        JPanel center = new JPanel(new BorderLayout(12,12));
        center.setBorder(new EmptyBorder(0,8,0,8));

        JPanel detail = new JPanel(new GridLayout(3,2,10,10));
        detail.setBorder(new EmptyBorder(8,8,8,8));
        detail.add(new JLabel("Lokasi (Kota, Negara):"));
        detail.add(lblLocation);
        detail.add(new JLabel("Suhu (°C):"));
        detail.add(lblTemp);
        detail.add(new JLabel("Kelembapan (%):"));
        detail.add(lblHumidity);

        JPanel detail2 = new JPanel(new GridLayout(3,2,10,10));
        detail2.setBorder(new EmptyBorder(8,8,8,8));
        detail2.add(new JLabel("Angin (m/s):"));
        detail2.add(lblWind);
        detail2.add(new JLabel("Kondisi:"));
        detail2.add(lblCondition);
        detail2.add(new JLabel("Diperbarui:"));
        detail2.add(lblUpdated);

        JPanel detailWrap = new JPanel(new GridLayout(1,2,12,12));
        detailWrap.add(detail);
        detailWrap.add(detail2);

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setBorder(BorderFactory.createTitledBorder("Ikon Cuaca"));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(150,150));
        iconPanel.add(lblIcon, BorderLayout.CENTER);

        JPanel topCenter = new JPanel(new BorderLayout(12,12));
        topCenter.add(detailWrap, BorderLayout.CENTER);
        topCenter.add(iconPanel, BorderLayout.EAST);

        center.add(topCenter, BorderLayout.NORTH);

        // Tabel riwayat
        String[] cols = {"Waktu","Kota","Negara","Suhu (°C)","Kelembapan (%)","Angin (m/s)","Kondisi","Deskripsi"};
        tableModel = new DefaultTableModel(cols, 0){
            @Override public boolean isCellEditable(int row, int col){ return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Riwayat Data Cuaca"));
        center.add(sp, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Menu
        setJMenuBar(createMenu());

        // Events
        btnCheck.addActionListener(e -> checkWeatherAction());
        btnAddFav.addActionListener(e -> addFavoriteAction());
        btnSaveCsv.addActionListener(e -> saveCsvAction());
        btnLoadCsv.addActionListener(e -> loadCsvAction());
        cbFavorites.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String city = (String) cbFavorites.getSelectedItem();
                if (city != null && !city.trim().isEmpty()) {
                    txtCity.setText(city);
                }
            }
        });

        txtCity.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    checkWeatherAction();
                }
            }
        });

        // Load API key & favorites
        apiKey = loadApiKeyOrPrompt();
        loadFavoritesToCombo();
    }

    // ===== Menu Bar =====
    private JMenuBar createMenu(){
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem miSave = new JMenuItem("Simpan CSV");
        miSave.addActionListener(e -> saveCsvAction());
        JMenuItem miLoad = new JMenuItem("Muat CSV");
        miLoad.addActionListener(e -> loadCsvAction());
        JMenuItem miExit = new JMenuItem("Keluar");
        miExit.addActionListener(e -> dispose());
        file.add(miSave); file.add(miLoad); file.addSeparator(); file.add(miExit);

        JMenu settings = new JMenu("Pengaturan");
        JMenuItem miApi = new JMenuItem("Ubah API Key…");
        miApi.addActionListener(e -> {
            String key = JOptionPane.showInputDialog(this, "Masukkan API Key OpenWeatherMap:", apiKey);
            if (key != null && !key.trim().isEmpty()){
                apiKey = key.trim();
                saveApiKey(apiKey);
                JOptionPane.showMessageDialog(this, "API Key disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        settings.add(miApi);

        mb.add(file);
        mb.add(settings);
        return mb;
    }

    // ===== Actions =====
    private void checkWeatherAction(){
        String city = txtCity.getText();
        if (city == null || city.trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Masukkan nama kota terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (apiKey == null || apiKey.trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "API Key belum diset. Buka menu Pengaturan → Ubah API Key…", "API Key", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            WeatherData data = fetchWeather(city.trim(), apiKey);
            if (data == null){
                JOptionPane.showMessageDialog(this, "Gagal mengambil data cuaca.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            renderWeather(data);
            appendTable(data);
        } catch (IOException ex){
            JOptionPane.showMessageDialog(this, "Gagal terhubung: " + ex.getMessage(), "Jaringan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFavoriteAction(){
        String city = txtCity.getText();
        if (city == null || city.trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Isi nama kota dulu.", "Favorit", JOptionPane.WARNING_MESSAGE);
            return;
        }
        city = normalizeCity(city);
        // Cegah duplikat (case-insensitive)
        for (int i=0;i<cbFavorites.getItemCount();i++){
            if (cbFavorites.getItemAt(i).equalsIgnoreCase(city)){
                JOptionPane.showMessageDialog(this, "Kota sudah ada di favorit.", "Favorit", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        cbFavorites.addItem(city);
        saveFavoritesFromCombo();
        JOptionPane.showMessageDialog(this, "Ditambahkan ke favorit: " + city, "Favorit", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveCsvAction(){
        if (tableModel.getRowCount()==0){
            JOptionPane.showMessageDialog(this, "Belum ada data untuk disimpan.", "CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("riwayat_cuaca.csv"));
        int res = ch.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION){
            File f = ch.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))){
                // Header
                for (int c=0;c<tableModel.getColumnCount();c++){
                    pw.print(escapeCsv(tableModel.getColumnName(c)));
                    if (c < tableModel.getColumnCount()-1) pw.print(",");
                }
                pw.println();
                // Rows
                for (int r=0;r<tableModel.getRowCount();r++){
                    for (int c=0;c<tableModel.getColumnCount();c++){
                        Object val = tableModel.getValueAt(r,c);
                        pw.print(escapeCsv(val==null? "" : val.toString()));
                        if (c < tableModel.getColumnCount()-1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "CSV disimpan ke:\n" + f.getAbsolutePath(), "CSV", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex){
                JOptionPane.showMessageDialog(this, "Gagal simpan CSV: " + ex.getMessage(), "CSV", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCsvAction(){
        JFileChooser ch = new JFileChooser();
        int res = ch.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION){
            File f = ch.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))){
                String header = br.readLine(); // skip header
                if (header == null){
                    JOptionPane.showMessageDialog(this, "File kosong.", "CSV", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // clear table
                tableModel.setRowCount(0);
                String line;
                while ((line = br.readLine()) != null){
                    String[] cols = parseCsvLine(line, tableModel.getColumnCount());
                    tableModel.addRow(cols);
                }
                JOptionPane.showMessageDialog(this, "CSV dimuat dari:\n" + f.getAbsolutePath(), "CSV", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex){
                JOptionPane.showMessageDialog(this, "Gagal muat CSV: " + ex.getMessage(), "CSV", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Render & Table =====
    private void renderWeather(WeatherData d){
        lblLocation.setText(d.city + ", " + d.country);
        lblTemp.setText(String.format(Locale.US, "%.1f", d.temp));
        lblHumidity.setText(String.valueOf(d.humidity));
        lblWind.setText(String.format(Locale.US, "%.1f", d.wind));
        lblCondition.setText(d.main + " (" + d.description + ")");
        lblUpdated.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(d.timestamp*1000)));

        // ikon
        if (d.iconCode != null){
            try {
                URL iconUrl = new URL("https://openweathermap.org/img/wn/" + d.iconCode + "@2x.png");
                Image img = ImageIO.read(iconUrl);
                if (img != null){
                    Image scaled = img.getScaledInstance(120,120, Image.SCALE_SMOOTH);
                    lblIcon.setIcon(new ImageIcon(scaled));
                } else {
                    lblIcon.setIcon(null);
                }
            } catch (IOException e){
                lblIcon.setIcon(null);
            }
        } else {
            lblIcon.setIcon(null);
        }
    }

    private void appendTable(WeatherData d){
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(d.timestamp*1000));
        tableModel.addRow(new Object[]{
                time, d.city, d.country,
                String.format(Locale.US, "%.1f", d.temp),
                d.humidity,
                String.format(Locale.US, "%.1f", d.wind),
                d.main, d.description
        });
    }

    // ===== Networking & Parsing (tanpa library JSON) =====
    private WeatherData fetchWeather(String city, String apiKey) throws IOException {
        String q = URLEncoder.encode(city, "UTF-8");
        String url = "https://api.openweathermap.org/data/2.5/weather?q="+q+"&appid="+apiKey+"&units=metric&lang=id";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");

        int code = conn.getResponseCode();
        if (code != 200){
            String err = readStream(conn.getErrorStream());
            throw new IOException("HTTP " + code + ": " + err);
        }
        String json = readStream(conn.getInputStream());
        conn.disconnect();

        // Ambil field penting dengan regex sederhana
        WeatherData d = new WeatherData();
        d.city = extractString(json, "\"name\"\\s*:\\s*\"(.*?)\"");
        d.country = extractString(json, "\"country\"\\s*:\\s*\"(.*?)\"");
        d.temp = extractDouble(json, "\"temp\"\\s*:\\s*([-+]?\\d*\\.?\\d+)");
        d.humidity = (int) Math.round(extractDouble(json, "\"humidity\"\\s*:\\s*(\\d+)"));
        d.wind = extractDouble(json, "\"speed\"\\s*:\\s*([-+]?\\d*\\.?\\d+)");
        d.main = extractString(json, "\"main\"\\s*:\\s*\"(.*?)\""); // dari array weather[0]
        d.description = extractString(json, "\"description\"\\s*:\\s*\"(.*?)\"");
        d.iconCode = extractString(json, "\"icon\"\\s*:\\s*\"(.*?)\"");
        long ts = (long) extractDouble(json, "\"dt\"\\s*:\\s*(\\d+)");
        d.timestamp = (ts > 0 ? ts : System.currentTimeMillis()/1000);

        // Jika nama kota kosong, pakai input
        if (d.city == null || d.city.trim().isEmpty()){
            d.city = city;
        }
        return d;
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private static String extractString(String text, String regex){
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()){
            return unescapeJson(m.group(1));
        }
        return null;
    }

    private static double extractDouble(String text, String regex){
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()){
            try { return Double.parseDouble(m.group(1)); }
            catch (NumberFormatException ignored){}
        }
        return 0.0;
    }

    private static String unescapeJson(String s){
        if (s == null) return null;
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r");
    }

    // ===== API Key & Favorites =====
    private String loadApiKeyOrPrompt(){
        String key = loadApiKey();
        if (key == null || key.trim().isEmpty()){
            key = JOptionPane.showInputDialog(this,
                    "Masukkan API Key OpenWeatherMap:",
                    "API Key", JOptionPane.QUESTION_MESSAGE);
            if (key != null && !key.trim().isEmpty()){
                key = key.trim();
                saveApiKey(key);
            } else {
                key = "";
            }
        }
        return key;
    }

    private String loadApiKey(){
        if (!API_KEY_FILE.exists()) return null;
        try {
            return new String(java.nio.file.Files.readAllBytes(API_KEY_FILE.toPath()), StandardCharsets.UTF_8).trim();
        } catch (IOException e){
            return null;
        }
    }

    private void saveApiKey(String key){
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(API_KEY_FILE), StandardCharsets.UTF_8))){
            pw.print(key == null ? "" : key.trim());
        } catch (IOException ignored){}
    }

    private void loadFavoritesToCombo(){
        cbFavorites.removeAllItems();
        if (!FAV_FILE.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FAV_FILE), StandardCharsets.UTF_8))){
            String line;
            while ((line = br.readLine()) != null){
                line = line.trim();
                if (!line.isEmpty()) cbFavorites.addItem(line);
            }
        } catch (IOException ignored){}
    }

    private void saveFavoritesFromCombo(){
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FAV_FILE), StandardCharsets.UTF_8))){
            for (int i=0;i<cbFavorites.getItemCount();i++){
                pw.println(cbFavorites.getItemAt(i));
            }
        } catch (IOException ignored){}
    }

    private static String normalizeCity(String s){
        return s.trim().replaceAll("\\s{2,}"," ");
    }

    // ===== CSV helpers =====
    private static String escapeCsv(String v){
        String s = v.replace("\"","\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")){
            return "\"" + s + "\"";
        }
        return s;
    }

    private static String[] parseCsvLine(String line, int expectedCols){
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (ch=='\"'){
                if (inQuotes && i+1<line.length() && line.charAt(i+1)=='\"'){
                    sb.append('\"'); i++; // escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch==',' && !inQuotes){
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        out.add(sb.toString());
        while (out.size() < expectedCols) out.add("");
        return out.toArray(new String[0]);
    }

    // ===== Model =====
    private static class WeatherData {
        String city;
        String country;
        double temp;
        int humidity;
        double wind;
        String main;
        String description;
        String iconCode;
        long timestamp;
    }

    // ===== Main =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
            new Tugas6().setVisible(true);
        });
    }
}

