package pbo2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tugas5 extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTextArea textArea = new JTextArea();
    private final JLabel lblWords = new JLabel("Kata: 0");
    private final JLabel lblChars = new JLabel("Karakter (termasuk spasi): 0");
    private final JLabel lblCharsNoSpace = new JLabel("Karakter (tanpa spasi): 0");
    private final JLabel lblSentences = new JLabel("Kalimat: 0");
    private final JLabel lblParagraphs = new JLabel("Paragraf: 0");

    private final JTextField txtSearch = new JTextField();
    private final JButton btnSearch = new JButton("Cari");
    private final JButton btnCount = new JButton("Hitung");
    private final JButton btnClearHighlights = new JButton("Bersihkan Sorot");

    private final Highlighter highlighter;
    private final Highlighter.HighlightPainter painter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 0, 160));

    private final Locale localeID = new Locale("id", "ID");

    public Tugas5() {
        super("Aplikasi Penghitung Kata");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // === TEXT AREA ===
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new EmptyBorder(8, 8, 8, 8));
        add(scrollPane, BorderLayout.CENTER);
        highlighter = textArea.getHighlighter();

        // === TOP BAR (Search) ===
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        txtSearch.setToolTipText("Masukkan kata/frasa yang ingin dicari (case-insensitive)");
        searchPanel.add(new JLabel("Cari kata:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchButtons.add(btnSearch);
        searchButtons.add(btnClearHighlights);
        searchPanel.add(searchButtons, BorderLayout.EAST);
        topBar.add(searchPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // === BOTTOM BAR (Stats + Actions) ===
        JPanel bottomBar = new JPanel(new BorderLayout());
        JPanel statsPanel = new JPanel(new GridLayout(1, 0, 12, 0));
        statsPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        statsPanel.add(lblWords);
        statsPanel.add(lblChars);
        statsPanel.add(lblCharsNoSpace);
        statsPanel.add(lblSentences);
        statsPanel.add(lblParagraphs);
        bottomBar.add(statsPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.add(btnCount);
        JButton btnSave = new JButton("Simpan ke File");
        actions.add(btnSave);
        bottomBar.add(actions, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        // === MENU ===
        setJMenuBar(createMenuBar(btnSave));

        // === EVENTS ===
        btnCount.addActionListener(this::onCount);
        btnSearch.addActionListener(this::onSearch);
        btnClearHighlights.addActionListener(e -> clearHighlights());

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStats(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStats(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStats(); }
        });

        // Tekan Enter di field pencarian untuk cari
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSearch(null);
                }
            }
        });

        // Hitung awal
        updateStats();
    }

    private JMenuBar createMenuBar(JButton btnSave) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem miSave = new JMenuItem(new AbstractAction("Simpan…") {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { saveToFile(); }
        });
        JMenuItem miExit = new JMenuItem(new AbstractAction("Keluar") {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
        fileMenu.add(miSave);
        fileMenu.addSeparator();
        fileMenu.add(miExit);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem miClearHighlights = new JMenuItem(new AbstractAction("Bersihkan Sorot") {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { clearHighlights(); }
        });
        editMenu.add(miClearHighlights);

        JMenu toolsMenu = new JMenu("Alat");
        JMenuItem miCount = new JMenuItem(new AbstractAction("Hitung Sekarang") {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { onCount(e); }
        });
        toolsMenu.add(miCount);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(toolsMenu);
        return menuBar;
    }

    private void onCount(ActionEvent e) {
        updateStats();
        JOptionPane.showMessageDialog(this, "Perhitungan diperbarui.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSearch(ActionEvent e) {
        clearHighlights();
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kata/frasa yang ingin dicari.", "Pencarian", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String content = textArea.getText();
        // Case-insensitive search
        Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher m = pattern.matcher(content);
        int hits = 0;
        while (m.find()) {
            try {
                highlighter.addHighlight(m.start(), m.end(), painter);
                hits++;
            } catch (BadLocationException ex) {
                // ignore
            }
        }
        if (hits == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ditemukan kemunculan kata/frasa: " + query, "Pencarian", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearHighlights() {
        highlighter.removeAllHighlights();
    }

    private void updateStats() {
        String text = textArea.getText();
        int charCount = text.length();
        int charNoSpace = text.replaceAll("\\s+", "").length();
        int wordCount = countWords(text);
        int sentenceCount = countSentences(text);
        int paragraphCount = countParagraphs(text);

        lblWords.setText("Kata: " + wordCount);
        lblChars.setText("Karakter (termasuk spasi): " + charCount);
        lblCharsNoSpace.setText("Karakter (tanpa spasi): " + charNoSpace);
        lblSentences.setText("Kalimat: " + sentenceCount);
        lblParagraphs.setText("Paragraf: " + paragraphCount);
    }

    /**
     * Hitung kata menggunakan BreakIterator agar andal lintas bahasa.
     * Hanya token berisi huruf/angka yang dihitung sebagai kata.
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        BreakIterator wordIt = BreakIterator.getWordInstance(localeID);
        wordIt.setText(text);
        int count = 0;
        int start = wordIt.first();
        for (int end = wordIt.next(); end != BreakIterator.DONE; start = end, end = wordIt.next()) {
            String token = text.substring(start, end).trim();
            if (token.isEmpty()) continue;
            if (token.codePoints().anyMatch(Character::isLetterOrDigit)) {
                count++;
            }
        }
        return count;
    }

    private int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        BreakIterator sentIt = BreakIterator.getSentenceInstance(localeID);
        sentIt.setText(text);
        int count = 0;
        int start = sentIt.first();
        for (int end = sentIt.next(); end != BreakIterator.DONE; start = end, end = sentIt.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) count++;
        }
        return count;
    }

    private int countParagraphs(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        // Paragraf dipisah oleh setidaknya satu baris kosong
        String[] parts = trimmed.split("(?:\\r?\\n){2,}");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isEmpty()) count++;
        }
        return count;
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Teks dan Hasil Perhitungan");
        chooser.setSelectedFile(new File("hasil_penghitungan.txt"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                List<String> lines = new ArrayList<>();
                lines.add("=== HASIL PENGHITUNGAN ===");
                lines.add(lblWords.getText());
                lines.add(lblChars.getText());
                lines.add(lblCharsNoSpace.getText());
                lines.add(lblSentences.getText());
                lines.add(lblParagraphs.getText());
                lines.add("");
                lines.add("=== TEKS ASLI ===");
                lines.add(textArea.getText());
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Berhasil disimpan ke:\n" + file.getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Gunakan look and feel sistem agar lebih konsisten
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) { }
            new Tugas5().setVisible(true);
        });
    }
}
