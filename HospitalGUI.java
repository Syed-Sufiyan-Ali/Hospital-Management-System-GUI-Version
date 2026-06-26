
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

// ===================== GRADIENT PANEL =====================
class GradientPanel extends JPanel {
    private Color c1, c2;
    private boolean vertical;
    GradientPanel(Color c1, Color c2) { this(c1, c2, false); }
    GradientPanel(Color c1, Color c2, boolean vertical) {
        this.c1=c1; this.c2=c2; this.vertical=vertical; setOpaque(false);
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (vertical)
            g2.setPaint(new GradientPaint(0,0,c1,0,getHeight(),c2));
        else
            g2.setPaint(new GradientPaint(0,0,c1,getWidth(),getHeight(),c2));
        g2.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }
}

// ===================== ROUNDED PANEL =====================
class RoundedPanel extends JPanel {
    private int radius; private Color bg;
    RoundedPanel(int radius, Color bg) { this.radius=radius; this.bg=bg; setOpaque(false); }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
        g2.dispose();
        super.paintComponent(g);
    }
}

// ===================== MODERN BUTTON =====================
class ModernButton extends JButton {
    private Color base, hover, pressed;
    private boolean isHovered=false, isPressed=false;
    ModernButton(String text, Color base) {
        super(text); this.base=base; hover=base.brighter(); pressed=base.darker();
        setFont(new Font("Segoe UI",Font.BOLD,13)); setForeground(Color.WHITE);
        setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
        setOpaque(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ isHovered=true; repaint(); }
            public void mouseExited(MouseEvent e) { isHovered=false; repaint(); }
            public void mousePressed(MouseEvent e){ isPressed=true; repaint(); }
            public void mouseReleased(MouseEvent e){ isPressed=false; repaint(); }
        });
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color draw=isPressed?pressed:(isHovered?hover:base);
        g2.setColor(draw); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
        g2.setColor(new Color(255,255,255,40)); g2.fillRoundRect(0,0,getWidth(),getHeight()/2,12,12);
        g2.dispose(); super.paintComponent(g);
    }
}

// ===================== MODERN FIELD =====================
class ModernField extends JTextField {
    private String placeholder;
    ModernField(String ph) {
        placeholder=ph; setFont(new Font("Segoe UI",Font.PLAIN,14));
        setForeground(new Color(30,30,30)); setBackground(new Color(248,250,255));
        setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(200,210,230),10),
            BorderFactory.createEmptyBorder(10,14,10,14))); setOpaque(true);
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty()&&!isFocusOwner()) {
            Graphics2D g2=(Graphics2D)g;
            g2.setColor(new Color(170,180,200));
            g2.setFont(new Font("Segoe UI",Font.ITALIC,13));
            Insets ins=getInsets();
            g2.drawString(placeholder,ins.left,getHeight()-ins.bottom-5);
        }
    }
}

// ===================== MODERN PASSWORD FIELD =====================
class ModernPass extends JPasswordField {
    ModernPass() {
        setFont(new Font("Segoe UI",Font.PLAIN,14));
        setForeground(new Color(30,30,30)); setBackground(new Color(248,250,255));
        setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(200,210,230),10),
            BorderFactory.createEmptyBorder(10,14,10,14))); setOpaque(true);
    }
}

// ===================== ROUND BORDER =====================
class RoundBorder extends AbstractBorder {
    private Color color; private int radius;
    RoundBorder(Color c,int r){ color=c; radius=r; }
    public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color); g2.drawRoundRect(x,y,w-1,h-1,radius,radius); g2.dispose();
    }
    public Insets getBorderInsets(Component c){ return new Insets(radius/2,radius/2,radius/2,radius/2); }
    public Insets getBorderInsets(Component c,Insets in){ in.set(radius/2,radius/2,radius/2,radius/2); return in; }
}

// ===================== AUTH MANAGER =====================
class AuthManager {
    private static final String AUTH_FILE = "auth.dat";

    static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return input; }
    }

    // Returns: null = ok, else error message
    static String login(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return "Username and password are required.";
        File f = new File(AUTH_FILE);
        if (!f.exists()) {
            // First run: create default admin account
            saveUser("admin", sha256("admin123"), "Administrator", "Admin");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4 && p[0].equalsIgnoreCase(username) && p[1].equals(sha256(password))) {
                    if (p.length >= 5 && p[4].equals("0")) return "ACCOUNT_DISABLED";
                    return null; // success
                }
            }
        } catch (IOException e) {}
        return "Invalid username or password.";
    }

    static String getDisplayName(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 3 && p[0].equalsIgnoreCase(username)) return p[2];
            }
        } catch (IOException e) {}
        return username;
    }

    static String getRole(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4 && p[0].equalsIgnoreCase(username)) return p[3];
            }
        } catch (IOException e) {}
        return "Staff";
    }

    static void saveUser(String username, String hashedPw, String displayName, String role) {
        // Load existing, replace or append
        java.util.List<String> lines = new java.util.ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 1 && p[0].equalsIgnoreCase(username)) {
                    lines.add(username+"|"+hashedPw+"|"+displayName+"|"+role+"|1");
                    found = true;
                } else lines.add(line);
            }
        } catch (IOException e) {}
        if (!found) lines.add(username+"|"+hashedPw+"|"+displayName+"|"+role+"|1");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AUTH_FILE))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) {}
    }

    static java.util.List<String[]> getAllUsers() {
        java.util.List<String[]> users = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) users.add(p);
            }
        } catch (IOException e) {}
        return users;
    }

    static boolean userExists(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 1 && p[0].equalsIgnoreCase(username)) return true;
            }
        } catch (IOException e) {}
        return false;
    }

    static void deleteUser(String username) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (!(p.length >= 1 && p[0].equalsIgnoreCase(username))) lines.add(line);
            }
        } catch (IOException e) {}
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AUTH_FILE))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) {}
    }
}

// ===================== LOGIN SCREEN =====================
class LoginFrame extends JFrame {
    private ModernField fUser;
    private ModernPass fPass;
    private JLabel errLabel;
    private JCheckBox showPass;
    private int attempts = 0;

    LoginFrame() {
        setTitle("MedCare HMS — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());

        // LEFT PANEL — branding
        GradientPanel left = new GradientPanel(new Color(30,27,75), new Color(67,56,202));
        left.setLayout(new BorderLayout());
        left.setPreferredSize(new Dimension(380, 0));

        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setOpaque(false);
        leftContent.setBorder(BorderFactory.createEmptyBorder(60,50,60,50));

        JLabel cross = new JLabel("✚");
        cross.setFont(new Font("Segoe UI",Font.BOLD,52));
        cross.setForeground(new Color(165,180,252));
        cross.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("MedCare");
        brand.setFont(new Font("Segoe UI",Font.BOLD,36));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Hospital Management System");
        brandSub.setFont(new Font("Segoe UI",Font.PLAIN,14));
        brandSub.setForeground(new Color(199,210,254));
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Feature bullets
        leftContent.add(cross);
        leftContent.add(Box.createVerticalStrut(16));
        leftContent.add(brand);
        leftContent.add(Box.createVerticalStrut(4));
        leftContent.add(brandSub);
        leftContent.add(Box.createVerticalStrut(40));

        String[] features = {"Patient Registration & Records","Smart Search & Filtering","Detailed Invoice & Billing","Role-Based Access Control"};
        for (String f : features) {
            JPanel row = new JPanel(new BorderLayout(10,0));
            row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
            JLabel dot = new JLabel("✓"); dot.setFont(new Font("Segoe UI",Font.BOLD,13));
            dot.setForeground(new Color(110,231,183)); dot.setPreferredSize(new Dimension(20,0));
            JLabel txt = new JLabel(f); txt.setFont(new Font("Segoe UI",Font.PLAIN,13));
            txt.setForeground(new Color(199,210,254));
            row.add(dot,BorderLayout.WEST); row.add(txt,BorderLayout.CENTER);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            leftContent.add(row); leftContent.add(Box.createVerticalStrut(10));
        }
        leftContent.add(Box.createVerticalGlue());

        JLabel version = new JLabel("v2.0  •  Secure Edition");
        version.setFont(new Font("Segoe UI",Font.PLAIN,11));
        version.setForeground(new Color(99,102,241));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftContent.add(version);

        left.add(leftContent, BorderLayout.CENTER);

        // RIGHT PANEL — login form
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(248,250,255));

        RoundedPanel card = new RoundedPanel(20, Color.WHITE) {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(40,44,40,44));
            setPreferredSize(new Dimension(360, 420));
        }};

        JLabel title = new JLabel("Welcome back");
        title.setFont(new Font("Segoe UI",Font.BOLD,26));
        title.setForeground(new Color(17,24,39));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Segoe UI",Font.PLAIN,14));
        subtitle.setForeground(new Color(107,114,128));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        fUser = new ModernField("Username");
        fUser.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        fUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        fPass = new ModernPass();
        fPass.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        fPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPass = new JCheckBox("Show password");
        showPass.setFont(new Font("Segoe UI",Font.PLAIN,12));
        showPass.setForeground(new Color(107,114,128));
        showPass.setOpaque(false);
        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.addActionListener(e ->
            fPass.setEchoChar(showPass.isSelected() ? (char)0 : '●'));
        fPass.setEchoChar('●');

        ModernButton btnLogin = new ModernButton("Sign In", new Color(79,70,229));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setFont(new Font("Segoe UI",Font.BOLD,15));

        errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
        errLabel.setForeground(new Color(239,68,68));
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Default: admin / admin123");
        hint.setFont(new Font("Segoe UI",Font.ITALIC,11));
        hint.setForeground(new Color(156,163,175));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        ActionListener doLogin = e -> attemptLogin();
        btnLogin.addActionListener(doLogin);
        fPass.addActionListener(doLogin);
        fUser.addActionListener(e -> fPass.requestFocus());

        JLabel uLbl = fieldLabel("Username");
        JLabel pLbl = fieldLabel("Password");

        card.add(title); card.add(Box.createVerticalStrut(4));
        card.add(subtitle); card.add(Box.createVerticalStrut(32));
        card.add(uLbl); card.add(Box.createVerticalStrut(6));
        card.add(fUser); card.add(Box.createVerticalStrut(16));
        card.add(pLbl); card.add(Box.createVerticalStrut(6));
        card.add(fPass); card.add(Box.createVerticalStrut(8));
        card.add(showPass); card.add(Box.createVerticalStrut(8));
        card.add(errLabel); card.add(Box.createVerticalStrut(4));
        card.add(btnLogin); card.add(Box.createVerticalStrut(16));
        card.add(hint);

        right.add(card);

        root.add(left,  BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        SwingUtilities.invokeLater(() -> fUser.requestFocus());
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(new Color(55,65,81));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void attemptLogin() {
        if (attempts >= 5) {
            errLabel.setText("Too many failed attempts. Restart the app.");
            return;
        }
        String user = fUser.getText().trim();
        String pass = new String(fPass.getPassword());
        String result = AuthManager.login(user, pass);
        if (result == null) {
            // Success
            String displayName = AuthManager.getDisplayName(user);
            String role        = AuthManager.getRole(user);
            dispose();
            SwingUtilities.invokeLater(() -> new HospitalGUI(displayName, user, role).setVisible(true));
        } else if (result.equals("ACCOUNT_DISABLED")) {
            errLabel.setText("This account has been disabled.");
        } else {
            attempts++;
            int left = 5 - attempts;
            errLabel.setText(result + (left < 5 ? "  (" + left + " attempts left)" : ""));
            fPass.setText("");
            fPass.requestFocus();
        }
    }
}

// ===================== BASE PANEL =====================
abstract class BasePanel extends JPanel {
    protected static String[][] patients = new String[100][5];
    protected static int totalPatients = 0;
    protected static final String FILE = "patients.txt";

    static final Color BG        = new Color(240,244,255);
    static final Color CARD_BG   = Color.WHITE;
    static final Color ACCENT    = new Color(79,70,229);
    static final Color ACCENT2   = new Color(16,185,129);
    static final Color DANGER    = new Color(239,68,68);
    static final Color WARNING   = new Color(245,158,11);
    static final Color TEXT_DARK = new Color(17,24,39);
    static final Color TEXT_MID  = new Color(75,85,99);
    static final Color TEXT_LITE = new Color(156,163,175);

    BasePanel() { setBackground(BG); }

    protected void loadPatients() {
        totalPatients=0;
        try (BufferedReader br=new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line=br.readLine())!=null && totalPatients<100) {
                String[] p=line.split("\\|");
                if (p.length==5) patients[totalPatients++]=p;
            }
        } catch (IOException e) {}
    }

    protected void savePatients() {
        try (BufferedWriter bw=new BufferedWriter(new FileWriter(FILE))) {
            for (int i=0;i<totalPatients;i++){ bw.write(String.join("|",patients[i])); bw.newLine(); }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"Error saving!","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    protected JLabel makeHeading(String title, String sub) {
        return new JLabel("<html><span style='font-size:18px;font-weight:bold;color:#111827'>"+title+
            "</span><br><span style='font-size:11px;color:#6B7280'>"+sub+"</span></html>");
    }

    protected RoundedPanel makeCard() {
        RoundedPanel p=new RoundedPanel(16,CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        return p;
    }

    abstract void refresh();
}

// ===================== VIEW PANEL =====================
class ViewPanel extends BasePanel {
    private DefaultTableModel model;
    private JLabel statLabel;

    ViewPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel topBar=new JPanel(new BorderLayout()); topBar.setOpaque(false);
        topBar.add(makeHeading("Patient Records","View and manage all registered patients"),BorderLayout.WEST);
        ModernButton btnR=new ModernButton("↻  Refresh",ACCENT);
        btnR.setPreferredSize(new Dimension(120,36));
        btnR.addActionListener(e->refresh()); topBar.add(btnR,BorderLayout.EAST);

        String[] cols={"Patient Name","Age","Gender","Patient ID","Diagnosis"};
        model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model);
        table.setRowHeight(40); table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setShowVerticalLines(false); table.setIntercellSpacing(new Dimension(0,4));
        table.setBackground(CARD_BG); table.setSelectionBackground(new Color(238,240,255));
        table.setSelectionForeground(TEXT_DARK); table.setFocusable(false);
        JTableHeader hdr=table.getTableHeader();
        hdr.setFont(new Font("Segoe UI",Font.BOLD,12)); hdr.setBackground(new Color(249,250,255));
        hdr.setForeground(new Color(99,102,241)); hdr.setPreferredSize(new Dimension(0,42));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,2,0,new Color(224,226,255)));
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (!sel){setBackground(row%2==0?CARD_BG:new Color(248,249,255));setForeground(TEXT_DARK);}
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12)); return this;
            }
        });
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(new RoundBorder(new Color(224,226,255),12));
        sp.setBackground(CARD_BG); sp.getViewport().setBackground(CARD_BG);

        statLabel=new JLabel("  Loading...");
        statLabel.setFont(new Font("Segoe UI",Font.PLAIN,12)); statLabel.setForeground(TEXT_LITE);

        add(topBar,BorderLayout.NORTH); add(sp,BorderLayout.CENTER); add(statLabel,BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        loadPatients(); model.setRowCount(0);
        for (int i=0;i<totalPatients;i++) model.addRow(patients[i]);
        statLabel.setText("  "+totalPatients+" patient"+(totalPatients!=1?"s":"")+" registered");
    }
}

// ===================== ADD PANEL =====================
class AddPanel extends BasePanel {
    private ModernField fName,fAge,fGender,fId,fIllness;
    private JLabel status;

    AddPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Add New Patient","Register a new patient into the system"),BorderLayout.NORTH);

        RoundedPanel card=makeCard(); card.setLayout(new BorderLayout(0,20));
        JPanel form=new JPanel(new GridLayout(5,2,16,14)); form.setOpaque(false);
        fName=new ModernField("e.g. Ahmed Khan"); fAge=new ModernField("e.g. 35");
        fGender=new ModernField("M or F"); fId=new ModernField("e.g. P-001");
        fIllness=new ModernField("e.g. Hypertension");
        Font lf=new Font("Segoe UI",Font.BOLD,13);
        String[] lbls={"Full Name","Age","Gender (M/F)","Patient ID","Diagnosis / Illness"};
        ModernField[] fields={fName,fAge,fGender,fId,fIllness};
        for (int i=0;i<lbls.length;i++){
            JLabel l=new JLabel(lbls[i]); l.setFont(lf); l.setForeground(TEXT_MID);
            form.add(l); form.add(fields[i]);
        }
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); btnRow.setOpaque(false);
        ModernButton btnAdd=new ModernButton("＋  Add Patient",ACCENT2);
        ModernButton btnClear=new ModernButton("↺  Clear",new Color(107,114,128));
        btnAdd.setPreferredSize(new Dimension(150,38)); btnClear.setPreferredSize(new Dimension(120,38));
        btnAdd.addActionListener(e->addPatient()); btnClear.addActionListener(e->clearFields());
        btnRow.add(btnAdd); btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(btnClear);
        status=new JLabel(" "); status.setFont(new Font("Segoe UI",Font.PLAIN,13));
        JPanel bottom=new JPanel(new BorderLayout(0,8)); bottom.setOpaque(false);
        bottom.add(btnRow,BorderLayout.NORTH); bottom.add(status,BorderLayout.SOUTH);
        card.add(form,BorderLayout.CENTER); card.add(bottom,BorderLayout.SOUTH);
        add(card,BorderLayout.CENTER);
    }

    private void addPatient() {
        loadPatients();
        if (totalPatients>=100){setStatus("System full!",DANGER);return;}
        String name=fName.getText().trim(),age=fAge.getText().trim();
        String gender=fGender.getText().trim().toUpperCase();
        String id=fId.getText().trim(),illness=fIllness.getText().trim();
        if (name.isEmpty()||age.isEmpty()||gender.isEmpty()||id.isEmpty()||illness.isEmpty()){
            setStatus("All fields are required!",DANGER);return;}
        for (int i=0;i<totalPatients;i++)
            if (patients[i][3].equalsIgnoreCase(id)){setStatus("Patient ID already exists!",DANGER);return;}
        patients[totalPatients++]=new String[]{name,age,gender,id,illness};
        savePatients(); setStatus("✓  Patient registered successfully!",ACCENT2); clearFields();
    }

    private void clearFields(){fName.setText("");fAge.setText("");fGender.setText("");fId.setText("");fIllness.setText("");}
    private void setStatus(String msg,Color c){status.setText("  "+msg);status.setForeground(c);}
    public void refresh(){}
}

// ===================== SEARCH PANEL =====================
class SearchPanel extends BasePanel {
    private DefaultTableModel model; private JLabel status;

    SearchPanel() {
        setLayout(new BorderLayout(0,16)); setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Search Patient","Find any patient by their unique ID"),BorderLayout.NORTH);
        RoundedPanel card=makeCard(); card.setLayout(new BorderLayout(0,16));
        ModernField fId=new ModernField("Enter Patient ID...");
        ModernButton btnS=new ModernButton("🔍  Search",ACCENT); btnS.setPreferredSize(new Dimension(130,38));
        JPanel bar=new JPanel(new BorderLayout(10,0)); bar.setOpaque(false);
        JLabel lbl=new JLabel("Patient ID"); lbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        lbl.setForeground(TEXT_MID); lbl.setPreferredSize(new Dimension(90,0));
        bar.add(lbl,BorderLayout.WEST); bar.add(fId,BorderLayout.CENTER); bar.add(btnS,BorderLayout.EAST);
        String[] cols={"Patient Name","Age","Gender","Patient ID","Diagnosis"};
        model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model);
        table.setRowHeight(40); table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setShowVerticalLines(false); table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(238,240,255));
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.getTableHeader().setBackground(new Color(249,250,255));
        table.getTableHeader().setForeground(new Color(99,102,241));
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){setBackground(CARD_BG);setForeground(TEXT_DARK);}
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));return this;
            }
        });
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(new RoundBorder(new Color(224,226,255),12)); sp.getViewport().setBackground(CARD_BG);
        status=new JLabel("  Enter a Patient ID and press Search");
        status.setFont(new Font("Segoe UI",Font.ITALIC,12)); status.setForeground(TEXT_LITE);
        ActionListener doSearch=e->{
            String id=fId.getText().trim();
            if (id.isEmpty()){status.setText("  Please enter a Patient ID");return;}
            loadPatients(); model.setRowCount(0); boolean found=false;
            for (int i=0;i<totalPatients;i++)
                if (patients[i][3].equalsIgnoreCase(id)){model.addRow(patients[i]);found=true;break;}
            status.setText(found?"  ✓ Patient found!":"  ✗ No patient found with ID: "+id);
            status.setForeground(found?ACCENT2:DANGER);
        };
        btnS.addActionListener(doSearch); fId.addActionListener(doSearch);
        card.add(bar,BorderLayout.NORTH); card.add(sp,BorderLayout.CENTER); card.add(status,BorderLayout.SOUTH);
        add(card,BorderLayout.CENTER);
    }
    public void refresh(){}
}

// ===================== DELETE PANEL =====================
class DeletePanel extends BasePanel {
    private JLabel status;

    DeletePanel() {
        setLayout(new BorderLayout(0,16)); setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Delete Patient","Permanently remove a patient record"),BorderLayout.NORTH);
        RoundedPanel card=makeCard(); card.setLayout(new BorderLayout(0,16));
        ModernField fId=new ModernField("Enter Patient ID to delete...");
        ModernButton btnD=new ModernButton("🗑  Delete Patient",DANGER); btnD.setPreferredSize(new Dimension(160,38));
        JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false);
        JLabel lbl=new JLabel("Patient ID"); lbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        lbl.setForeground(TEXT_MID); lbl.setPreferredSize(new Dimension(90,0));
        row.add(lbl,BorderLayout.WEST); row.add(fId,BorderLayout.CENTER); row.add(btnD,BorderLayout.EAST);
        RoundedPanel warn=new RoundedPanel(10,new Color(255,247,237));
        warn.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(new Color(253,186,116),10),BorderFactory.createEmptyBorder(12,16,12,16)));
        warn.setLayout(new BorderLayout());
        JLabel wt=new JLabel("<html>⚠️ &nbsp;<b>Warning:</b> This action is permanent and cannot be undone.</html>");
        wt.setFont(new Font("Segoe UI",Font.PLAIN,12)); wt.setForeground(new Color(146,64,14));
        warn.add(wt);
        status=new JLabel("  Enter a Patient ID above to delete");
        status.setFont(new Font("Segoe UI",Font.PLAIN,13)); status.setForeground(TEXT_LITE);
        btnD.addActionListener(e->{
            String id=fId.getText().trim();
            if (id.isEmpty()){status.setText("  Enter a Patient ID first!");status.setForeground(DANGER);return;}
            int c=JOptionPane.showConfirmDialog(this,"Permanently delete patient ID: "+id+"?","Confirm Deletion",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if (c!=JOptionPane.YES_OPTION) return;
            loadPatients(); boolean found=false;
            for (int i=0;i<totalPatients;i++){
                if (patients[i][3].equalsIgnoreCase(id)){
                    for (int j=i;j<totalPatients-1;j++) patients[j]=patients[j+1];
                    totalPatients--;found=true;break;
                }
            }
            if (found){savePatients();status.setText("  ✓ Patient deleted successfully!");status.setForeground(ACCENT2);fId.setText("");}
            else{status.setText("  ✗ No patient found with ID: "+id);status.setForeground(DANGER);}
        });
        card.add(row,BorderLayout.NORTH); card.add(warn,BorderLayout.CENTER); card.add(status,BorderLayout.SOUTH);
        add(card,BorderLayout.CENTER);
    }
    public void refresh(){}
}

// ===================== INVOICE DIALOG =====================
class InvoiceDialog extends JDialog {
    InvoiceDialog(Frame owner,String patientName,String patientId,String illness,
                  String age,String gender,int days,int ratePerDay){
        super(owner,"Invoice Receipt",true);
        setSize(520,720); setLocationRelativeTo(owner); setResizable(false);
        int roomCharge=days*ratePerDay,docFee=days*500,nursingFee=days*300,medCharges=days*200;
        int subtotal=roomCharge+docFee+nursingFee+medCharges;
        int tax=(int)(subtotal*0.05),total=subtotal+tax;
        String invoiceNo="INV-"+System.currentTimeMillis()%100000;
        String date=new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date());
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(Color.WHITE);
        GradientPanel header=new GradientPanel(new Color(79,70,229),new Color(99,102,241));
        header.setLayout(new BorderLayout()); header.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        header.setPreferredSize(new Dimension(0,130));
        JPanel hLeft=new JPanel(new GridLayout(4,1,0,2)); hLeft.setOpaque(false);
        JLabel hn=new JLabel("MedCare Hospital"); hn.setFont(new Font("Segoe UI",Font.BOLD,20)); hn.setForeground(Color.WHITE);
        JLabel hs=new JLabel("Advanced Healthcare Services"); hs.setFont(new Font("Segoe UI",Font.PLAIN,11)); hs.setForeground(new Color(199,210,254));
        JLabel iNo=new JLabel("Invoice # "+invoiceNo); iNo.setFont(new Font("Segoe UI",Font.BOLD,12)); iNo.setForeground(new Color(224,231,255));
        JLabel iDt=new JLabel(date); iDt.setFont(new Font("Segoe UI",Font.PLAIN,11)); iDt.setForeground(new Color(199,210,254));
        hLeft.add(hn); hLeft.add(hs); hLeft.add(iNo); hLeft.add(iDt);
        JLabel badge=new JLabel("RECEIPT",SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI",Font.BOLD,13)); badge.setForeground(new Color(79,70,229));
        badge.setBackground(Color.WHITE); badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(6,14,6,14)); badge.setPreferredSize(new Dimension(90,36));
        header.add(hLeft,BorderLayout.WEST); header.add(badge,BorderLayout.EAST);
        JPanel body=new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE); body.setBorder(BorderFactory.createEmptyBorder(20,28,20,28));
        body.add(sLbl("PATIENT INFORMATION")); body.add(Box.createVerticalStrut(8));
        JPanel patInfo=new JPanel(new GridLayout(2,4,8,6));
        patInfo.setBackground(new Color(249,250,255));
        patInfo.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(new Color(224,226,255),8),BorderFactory.createEmptyBorder(12,14,12,14)));
        patInfo.add(ii("Name",patientName)); patInfo.add(ii("ID",patientId));
        patInfo.add(ii("Age",age+" yrs")); patInfo.add(ii("Gender",gender.equals("M")?"Male":gender.equals("F")?"Female":gender));
        patInfo.add(ii("Diagnosis",illness)); patInfo.add(ii("Ward","General"));
        patInfo.add(ii("Days",days+(days==1?" day":" days"))); patInfo.add(ii("Status","Discharged"));
        body.add(patInfo); body.add(Box.createVerticalStrut(18));
        body.add(sLbl("BILLING BREAKDOWN")); body.add(Box.createVerticalStrut(8));
        JPanel bt=new JPanel(new GridBagLayout()); bt.setBackground(Color.WHITE);
        bt.setBorder(new RoundBorder(new Color(229,231,235),10));
        addBH(bt); addBR(bt,0,"Room & Board","Per day × "+days,ratePerDay,roomCharge,false);
        addBR(bt,1,"Doctor's Fee","Per day × "+days,500,docFee,false);
        addBR(bt,2,"Nursing Care","Per day × "+days,300,nursingFee,false);
        addBR(bt,3,"Medications","Per day × "+days,200,medCharges,true);
        body.add(bt); body.add(Box.createVerticalStrut(10));
        JPanel totals=new JPanel(new BorderLayout()); totals.setBackground(Color.WHITE);
        JPanel totInner=new JPanel(new GridLayout(3,2,0,4)); totInner.setOpaque(false);
        totInner.setBorder(BorderFactory.createEmptyBorder(0,200,0,0));
        addTR(totInner,"Subtotal","Rs. "+subtotal,false);
        addTR(totInner,"Tax (5%)","Rs. "+tax,false);
        addTR(totInner,"TOTAL DUE","Rs. "+total,true);
        totals.add(totInner,BorderLayout.EAST); body.add(totals);
        body.add(Box.createVerticalStrut(16));
        JLabel footer=new JLabel("<html><center><i>Thank you for choosing MedCare Hospital.<br>Billing inquiries: <b>021-111-MEDCARE</b></i></center></html>",SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI",Font.PLAIN,11)); footer.setForeground(new Color(156,163,175));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT); body.add(footer);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,10));
        actions.setBackground(new Color(249,250,255));
        actions.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));
        ModernButton btnP=new ModernButton("🖨  Print",new Color(79,70,229));
        ModernButton btnC=new ModernButton("✕  Close",new Color(107,114,128));
        btnP.setPreferredSize(new Dimension(110,36)); btnC.setPreferredSize(new Dimension(110,36));
        btnP.addActionListener(e->printInv(body)); btnC.addActionListener(e->dispose());
        actions.add(btnP); actions.add(btnC);
        JScrollPane scroll=new JScrollPane(body); scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(header,BorderLayout.NORTH); root.add(scroll,BorderLayout.CENTER); root.add(actions,BorderLayout.SOUTH);
        setContentPane(root);
    }
    private JPanel ii(String l,String v){
        JPanel p=new JPanel(new GridLayout(2,1,0,2)); p.setOpaque(false);
        JLabel a=new JLabel(l); a.setFont(new Font("Segoe UI",Font.PLAIN,10)); a.setForeground(new Color(156,163,175));
        JLabel b=new JLabel(v); b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setForeground(new Color(17,24,39));
        p.add(a); p.add(b); return p;
    }
    private JLabel sLbl(String t){
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,10));
        l.setForeground(new Color(99,102,241)); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    private void addBH(JPanel p){
        GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.gridy=0;
        String[] h={"Description","Rate","Qty","Amount"}; int[] w={180,80,100,100};
        for (int i=0;i<h.length;i++){
            JLabel l=new JLabel(h[i]); l.setFont(new Font("Segoe UI",Font.BOLD,11));
            l.setForeground(new Color(99,102,241)); l.setBackground(new Color(238,240,255));
            l.setOpaque(true); l.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
            l.setHorizontalAlignment(i>0?SwingConstants.RIGHT:SwingConstants.LEFT);
            gc.gridx=i; gc.weightx=(i==0)?1:0; gc.ipadx=w[i]-40; p.add(l,gc);
        }
    }
    private void addBR(JPanel p,int row,String d,String q,int r,int a,boolean last){
        GridBagConstraints gc=new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.gridy=row+1;
        Color bg=row%2==0?Color.WHITE:new Color(249,250,255);
        Border bot=last?BorderFactory.createEmptyBorder():BorderFactory.createMatteBorder(0,0,1,0,new Color(243,244,246));
        String[] v={d,"Rs."+r,q,"Rs."+a};
        for (int i=0;i<v.length;i++){
            JLabel l=new JLabel(v[i]); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(new Color(55,65,81));
            l.setBackground(bg); l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(bot,BorderFactory.createEmptyBorder(10,12,10,12)));
            l.setHorizontalAlignment(i>0?SwingConstants.RIGHT:SwingConstants.LEFT);
            gc.gridx=i; gc.weightx=(i==0)?1:0; p.add(l,gc);
        }
    }
    private void addTR(JPanel p,String l,String a,boolean bold){
        Font f=bold?new Font("Segoe UI",Font.BOLD,14):new Font("Segoe UI",Font.PLAIN,12);
        Color c=bold?new Color(79,70,229):new Color(55,65,81);
        JLabel la=new JLabel(l),lb=new JLabel(a);
        la.setFont(f); la.setForeground(c); lb.setFont(f); lb.setForeground(c);
        lb.setHorizontalAlignment(SwingConstants.RIGHT);
        if (bold){la.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));lb.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));}
        p.add(la); p.add(lb);
    }
    private void printInv(JPanel panel){
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setPrintable((g,pf,pi)->{
            if (pi>0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2=(Graphics2D)g;
            g2.translate(pf.getImageableX(),pf.getImageableY());
            double sx=pf.getImageableWidth()/panel.getWidth(),sy=pf.getImageableHeight()/panel.getHeight();
            g2.scale(Math.min(sx,sy),Math.min(sx,sy)); panel.printAll(g2); return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()){try{job.print();}catch(PrinterException ex){JOptionPane.showMessageDialog(this,"Print error: "+ex.getMessage());}}
    }
}

// ===================== BILL PANEL =====================
class BillPanel extends BasePanel {
    private ModernField fId,fDays;
    private JComboBox<String> roomType;
    private JLabel status;

    BillPanel() {
        setLayout(new BorderLayout(0,16)); setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Billing & Invoice","Generate detailed invoices for patient stays"),BorderLayout.NORTH);
        RoundedPanel card=makeCard(); card.setLayout(new BorderLayout(0,20));
        JPanel form=new JPanel(new GridLayout(3,2,16,14)); form.setOpaque(false);
        fId=new ModernField("Enter Patient ID..."); fDays=new ModernField("Number of days...");
        roomType=new JComboBox<>(new String[]{"General Ward  — Rs.1,000/day","Semi-Private — Rs.2,000/day","Private Room — Rs.3,500/day","ICU           — Rs.6,000/day"});
        roomType.setFont(new Font("Segoe UI",Font.PLAIN,13)); roomType.setBackground(new Color(250,251,255));
        Font lf=new Font("Segoe UI",Font.BOLD,13);
        JLabel l1=new JLabel("Patient ID"),l2=new JLabel("Days Admitted"),l3=new JLabel("Room Type");
        for (JLabel l:new JLabel[]{l1,l2,l3}){l.setFont(lf);l.setForeground(TEXT_MID);}
        form.add(l1); form.add(fId); form.add(l2); form.add(fDays); form.add(l3); form.add(roomType);
        ModernButton btnG=new ModernButton("📄  Generate Invoice",new Color(79,70,229));
        btnG.setPreferredSize(new Dimension(200,42));
        status=new JLabel("  Fill in the fields above and generate an invoice");
        status.setFont(new Font("Segoe UI",Font.ITALIC,12)); status.setForeground(TEXT_LITE);
        JPanel infoRow=new JPanel(new GridLayout(1,4,12,0)); infoRow.setOpaque(false);
        String[][] infos={{"1,000","General Ward /day"},{"2,000","Semi-Private /day"},{"3,500","Private Room /day"},{"6,000","ICU /day"}};
        Color[] ic={new Color(79,70,229),new Color(16,185,129),new Color(245,158,11),new Color(239,68,68)};
        for (int i=0;i<infos.length;i++){
            RoundedPanel ip=new RoundedPanel(12,ic[i]); ip.setLayout(new GridLayout(2,1,0,4));
            ip.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
            JLabel am=new JLabel("Rs."+infos[i][0]); am.setFont(new Font("Segoe UI",Font.BOLD,16)); am.setForeground(Color.WHITE);
            JLabel sb=new JLabel(infos[i][1]); sb.setFont(new Font("Segoe UI",Font.PLAIN,11)); sb.setForeground(new Color(255,255,255,200));
            ip.add(am); ip.add(sb); infoRow.add(ip);
        }
        btnG.addActionListener(e->{
            String id=fId.getText().trim(),ds=fDays.getText().trim();
            if (id.isEmpty()||ds.isEmpty()){setStatus("All fields are required!",DANGER);return;}
            try{
                int days=Integer.parseInt(ds);
                if (days<=0){setStatus("Days must be > 0!",DANGER);return;}
                loadPatients(); String[] found=null;
                for (int i=0;i<totalPatients;i++) if (patients[i][3].equalsIgnoreCase(id)){found=patients[i];break;}
                if (found==null){setStatus("✗ Patient not found: "+id,DANGER);return;}
                int[] rates={1000,2000,3500,6000};
                int rate=rates[roomType.getSelectedIndex()];
                InvoiceDialog dlg=new InvoiceDialog((Frame)SwingUtilities.getWindowAncestor(this),found[0],found[3],found[4],found[1],found[2],days,rate);
                dlg.setVisible(true);
                setStatus("✓ Invoice generated for "+found[0],ACCENT2);
            }catch(NumberFormatException ex){setStatus("Enter a valid number for days!",DANGER);}
        });
        JPanel btnArea=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); btnArea.setOpaque(false); btnArea.add(btnG);
        JPanel bottom=new JPanel(new BorderLayout(0,8)); bottom.setOpaque(false);
        bottom.add(btnArea,BorderLayout.NORTH); bottom.add(status,BorderLayout.SOUTH);
        card.add(form,BorderLayout.NORTH); card.add(infoRow,BorderLayout.CENTER); card.add(bottom,BorderLayout.SOUTH);
        add(card,BorderLayout.CENTER);
    }
    private void setStatus(String msg,Color c){status.setText("  "+msg);status.setForeground(c);}
    public void refresh(){}
}

// ===================== USER MANAGEMENT PANEL (Admin only) =====================
class UserPanel extends BasePanel {
    private DefaultTableModel model;
    private JLabel status;

    UserPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel topBar=new JPanel(new BorderLayout()); topBar.setOpaque(false);
        topBar.add(makeHeading("User Management","Create and manage system user accounts"),BorderLayout.WEST);
        ModernButton btnR=new ModernButton("↻  Refresh",ACCENT);
        btnR.setPreferredSize(new Dimension(120,36));
        btnR.addActionListener(e->refresh()); topBar.add(btnR,BorderLayout.EAST);

        // User table
        String[] cols={"Username","Display Name","Role","Status"};
        model=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model);
        table.setRowHeight(38); table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setShowVerticalLines(false); table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(238,240,255)); table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.getTableHeader().setBackground(new Color(249,250,255));
        table.getTableHeader().setForeground(new Color(99,102,241));
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (!sel){setBackground(row%2==0?CARD_BG:new Color(248,249,255));setForeground(TEXT_DARK);}
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12)); return this;
            }
        });
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(new RoundBorder(new Color(224,226,255),12)); sp.getViewport().setBackground(CARD_BG);

        // Add user form
        RoundedPanel addCard=makeCard(); addCard.setLayout(new BorderLayout(0,14));
        JLabel addTitle=new JLabel("Add / Update User");
        addTitle.setFont(new Font("Segoe UI",Font.BOLD,14)); addTitle.setForeground(TEXT_DARK);

        JPanel form=new JPanel(new GridLayout(2,4,10,10)); form.setOpaque(false);
        ModernField fUser=new ModernField("Username");
        ModernField fName=new ModernField("Display Name");
        ModernPass  fPass=new ModernPass();
        fPass.setToolTipText("Password");
        JComboBox<String> roleBox=new JComboBox<>(new String[]{"Admin","Doctor","Receptionist","Staff"});
        roleBox.setFont(new Font("Segoe UI",Font.PLAIN,13));

        Font lf=new Font("Segoe UI",Font.BOLD,12);
        JLabel lu=new JLabel("Username"); lu.setFont(lf); lu.setForeground(TEXT_MID);
        JLabel ln=new JLabel("Display Name"); ln.setFont(lf); ln.setForeground(TEXT_MID);
        JLabel lp=new JLabel("Password"); lp.setFont(lf); lp.setForeground(TEXT_MID);
        JLabel lr=new JLabel("Role"); lr.setFont(lf); lr.setForeground(TEXT_MID);

        form.add(lu); form.add(fUser); form.add(ln); form.add(fName);
        form.add(lp); form.add(fPass); form.add(lr); form.add(roleBox);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); btnRow.setOpaque(false);
        ModernButton btnAdd=new ModernButton("＋  Add User",ACCENT2);
        ModernButton btnDel=new ModernButton("🗑  Delete Selected",DANGER);
        btnAdd.setPreferredSize(new Dimension(150,36)); btnDel.setPreferredSize(new Dimension(180,36));
        btnRow.add(btnAdd); btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(btnDel);

        status=new JLabel(" "); status.setFont(new Font("Segoe UI",Font.PLAIN,13));

        btnAdd.addActionListener(e->{
            String u=fUser.getText().trim(), n=fName.getText().trim();
            String p=new String(fPass.getPassword());
            if (u.isEmpty()||n.isEmpty()||p.isEmpty()){setStatus("All fields required!",DANGER);return;}
            if (p.length()<6){setStatus("Password must be at least 6 characters!",DANGER);return;}
            String role=(String)roleBox.getSelectedItem();
            AuthManager.saveUser(u,AuthManager.sha256(p),n,role);
            setStatus("✓ User '"+u+"' saved!",ACCENT2);
            fUser.setText(""); fName.setText(""); fPass.setText("");
            refresh();
        });

        btnDel.addActionListener(e->{
            int row=table.getSelectedRow();
            if (row<0){setStatus("Select a user from the table first!",WARNING);return;}
            String u=(String)model.getValueAt(row,0);
            if (u.equalsIgnoreCase("admin")){setStatus("Cannot delete the default admin!",DANGER);return;}
            int c=JOptionPane.showConfirmDialog(this,"Delete user '"+u+"'?","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if (c!=JOptionPane.YES_OPTION) return;
            AuthManager.deleteUser(u);
            setStatus("✓ User '"+u+"' deleted!",ACCENT2); refresh();
        });

        addCard.add(addTitle,BorderLayout.NORTH);
        addCard.add(form,BorderLayout.CENTER);
        JPanel bot=new JPanel(new BorderLayout(0,6)); bot.setOpaque(false);
        bot.add(btnRow,BorderLayout.NORTH); bot.add(status,BorderLayout.SOUTH);
        addCard.add(bot,BorderLayout.SOUTH);

        JPanel center=new JPanel(new BorderLayout(0,16)); center.setOpaque(false);
        center.add(sp,BorderLayout.CENTER); center.add(addCard,BorderLayout.SOUTH);

        add(topBar,BorderLayout.NORTH); add(center,BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (String[] u : AuthManager.getAllUsers()) {
            String statusStr=(u.length>=5 && u[4].equals("0"))?"Disabled":"Active";
            model.addRow(new Object[]{u[0],u.length>=3?u[2]:"",u.length>=4?u[3]:"",statusStr});
        }
    }

    private void setStatus(String msg,Color c){status.setText("  "+msg);status.setForeground(c);}
}

// ===================== CHANGE PASSWORD DIALOG =====================
class ChangePasswordDialog extends JDialog {
    ChangePasswordDialog(Frame owner, String username) {
        super(owner,"Change Password",true);
        setSize(400,320); setLocationRelativeTo(owner); setResizable(false);

        RoundedPanel card=new RoundedPanel(16,Color.WHITE);
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(30,36,30,36));

        JLabel title=new JLabel("Change Password");
        title.setFont(new Font("Segoe UI",Font.BOLD,18)); title.setForeground(new Color(17,24,39));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernPass fOld=new ModernPass(); fOld.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        ModernPass fNew=new ModernPass(); fNew.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        ModernPass fCon=new ModernPass(); fCon.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));

        JLabel errLbl=new JLabel(" ");
        errLbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); errLbl.setForeground(new Color(239,68,68));
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernButton btnSave=new ModernButton("Update Password",new Color(79,70,229));
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font lf=new Font("Segoe UI",Font.BOLD,12); Color lc=new Color(55,65,81);
        JLabel l1=label("Current Password",lf,lc),l2=label("New Password",lf,lc),l3=label("Confirm Password",lf,lc);

        btnSave.addActionListener(e->{
            String oldP=new String(fOld.getPassword());
            String newP=new String(fNew.getPassword());
            String conP=new String(fCon.getPassword());
            if (oldP.isEmpty()||newP.isEmpty()||conP.isEmpty()){errLbl.setText("All fields required!");return;}
            if (AuthManager.login(username,oldP)!=null){errLbl.setText("Current password is incorrect!");return;}
            if (newP.length()<6){errLbl.setText("New password must be at least 6 characters!");return;}
            if (!newP.equals(conP)){errLbl.setText("Passwords do not match!");return;}
            AuthManager.saveUser(username,AuthManager.sha256(newP),AuthManager.getDisplayName(username),AuthManager.getRole(username));
            JOptionPane.showMessageDialog(this,"Password updated successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        card.add(title); card.add(Box.createVerticalStrut(20));
        card.add(l1); card.add(Box.createVerticalStrut(6)); card.add(fOld); card.add(Box.createVerticalStrut(12));
        card.add(l2); card.add(Box.createVerticalStrut(6)); card.add(fNew); card.add(Box.createVerticalStrut(12));
        card.add(l3); card.add(Box.createVerticalStrut(6)); card.add(fCon); card.add(Box.createVerticalStrut(10));
        card.add(errLbl); card.add(Box.createVerticalStrut(4)); card.add(btnSave);

        JPanel root=new JPanel(new GridBagLayout()); root.setBackground(new Color(240,244,255));
        root.add(card); setContentPane(root);
    }
    private JLabel label(String t,Font f,Color c){
        JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
}

// ===================== MAIN APP =====================
public class HospitalGUI extends JFrame {
    private String loggedInUser;
    private String loggedInRole;

    HospitalGUI(String displayName, String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;

        setTitle("MedCare HMS  —  " + displayName + "  [" + role + "]");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 660);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(820, 560));

        // ---- SIDEBAR ----
        GradientPanel sidebar=new GradientPanel(new Color(30,27,75),new Color(49,46,129));
        sidebar.setLayout(new BorderLayout()); sidebar.setPreferredSize(new Dimension(215,0));
        JPanel sideContent=new JPanel();
        sideContent.setLayout(new BoxLayout(sideContent,BoxLayout.Y_AXIS));
        sideContent.setOpaque(false); sideContent.setBorder(BorderFactory.createEmptyBorder(24,14,20,14));

        // Logo
        JPanel logoArea=new JPanel(new BorderLayout(0,3)); logoArea.setOpaque(false);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE,70));
        JLabel cross=new JLabel("✚",SwingConstants.CENTER);
        cross.setFont(new Font("Segoe UI",Font.BOLD,26)); cross.setForeground(new Color(165,180,252));
        JLabel hName=new JLabel("MedCare HMS",SwingConstants.CENTER);
        hName.setFont(new Font("Segoe UI",Font.BOLD,14)); hName.setForeground(Color.WHITE);
        JLabel hSub=new JLabel("Management System",SwingConstants.CENTER);
        hSub.setFont(new Font("Segoe UI",Font.PLAIN,10)); hSub.setForeground(new Color(148,163,184));
        logoArea.add(cross,BorderLayout.NORTH); logoArea.add(hName,BorderLayout.CENTER); logoArea.add(hSub,BorderLayout.SOUTH);
        sideContent.add(logoArea); sideContent.add(Box.createVerticalStrut(16));

        // User badge
        RoundedPanel userBadge=new RoundedPanel(10,new Color(255,255,255,20));
        userBadge.setLayout(new BorderLayout(10,0));
        userBadge.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        userBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));
        JLabel avatar=new JLabel("👤"); avatar.setFont(new Font("Segoe UI",Font.PLAIN,22));
        JPanel namePanel=new JPanel(new GridLayout(2,1,0,2)); namePanel.setOpaque(false);
        JLabel uName=new JLabel(displayName); uName.setFont(new Font("Segoe UI",Font.BOLD,12)); uName.setForeground(Color.WHITE);
        JLabel uRole=new JLabel(role); uRole.setFont(new Font("Segoe UI",Font.PLAIN,10)); uRole.setForeground(new Color(165,180,252));
        namePanel.add(uName); namePanel.add(uRole);
        userBadge.add(avatar,BorderLayout.WEST); userBadge.add(namePanel,BorderLayout.CENTER);
        sideContent.add(userBadge); sideContent.add(Box.createVerticalStrut(20));

        JSeparator sep=new JSeparator(); sep.setForeground(new Color(255,255,255,30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        sideContent.add(sep); sideContent.add(Box.createVerticalStrut(12));
        JLabel navLbl=new JLabel("  NAVIGATION");
        navLbl.setFont(new Font("Segoe UI",Font.BOLD,10)); navLbl.setForeground(new Color(148,163,184));
        navLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideContent.add(navLbl); sideContent.add(Box.createVerticalStrut(8));

        // Card panel
        CardLayout cards=new CardLayout();
        JPanel cardPanel=new JPanel(cards); cardPanel.setBackground(BasePanel.BG);

        ViewPanel   vp=new ViewPanel();
        AddPanel    ap=new AddPanel();
        SearchPanel sp2=new SearchPanel();
        BillPanel   bp=new BillPanel();
        DeletePanel dp=new DeletePanel();
        UserPanel   up=new UserPanel();

        cardPanel.add(vp,"view"); cardPanel.add(ap,"add"); cardPanel.add(sp2,"search");
        cardPanel.add(bp,"bill"); cardPanel.add(dp,"delete"); cardPanel.add(up,"users");

        // Nav items
        java.util.List<String[]> navItems=new java.util.ArrayList<>();
        navItems.add(new String[]{"View Patients","view","👥"});
        navItems.add(new String[]{"Add Patient","add","➕"});
        navItems.add(new String[]{"Search Patient","search","🔍"});
        navItems.add(new String[]{"Generate Bill","bill","📄"});
        navItems.add(new String[]{"Delete Patient","delete","🗑"});
        if (role.equals("Admin")) navItems.add(new String[]{"User Management","users","👤"});

        ButtonGroup grp=new ButtonGroup();
        java.util.List<JToggleButton> btnList=new java.util.ArrayList<>();

        for (String[] item : navItems) {
            final String key=item[1];
            JToggleButton btn=new JToggleButton(item[2]+"  "+item[0]){
                protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected()){
                        g2.setColor(new Color(255,255,255,25));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                        g2.setColor(new Color(165,180,252));
                        g2.fillRoundRect(0,(getHeight()-20)/2,3,20,3,3);
                    } else if (getModel().isRollover()){
                        g2.setColor(new Color(255,255,255,12));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
            btn.setForeground(new Color(148,163,184));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
            btn.addActionListener(e->{
                cards.show(cardPanel,key);
                for (JToggleButton b:btnList) b.setForeground(b.isSelected()?Color.WHITE:new Color(148,163,184));
            });
            grp.add(btn); btnList.add(btn);
            sideContent.add(btn); sideContent.add(Box.createVerticalStrut(4));
        }
        btnList.get(0).setSelected(true); btnList.get(0).setForeground(Color.WHITE);

        sideContent.add(Box.createVerticalGlue());
        JSeparator sep2=new JSeparator(); sep2.setForeground(new Color(255,255,255,30));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        sideContent.add(sep2); sideContent.add(Box.createVerticalStrut(8));

        // Bottom buttons
        JButton btnPwd=sideBtn("🔑  Change Password",new Color(165,180,252));
        JButton btnLogout=sideBtn("↩  Logout",new Color(252,165,165));
        JButton btnExit=sideBtn("⏻  Exit System",new Color(252,165,165));

        btnPwd.addActionListener(e->{
            new ChangePasswordDialog(this,loggedInUser).setVisible(true);
        });
        btnLogout.addActionListener(e->{
            int c=JOptionPane.showConfirmDialog(this,"Logout and return to login screen?","Logout",JOptionPane.YES_NO_OPTION);
            if (c==JOptionPane.YES_OPTION){
                dispose(); SwingUtilities.invokeLater(()->new LoginFrame().setVisible(true));
            }
        });
        btnExit.addActionListener(e->{
            int c=JOptionPane.showConfirmDialog(this,"Exit MedCare HMS?","Exit",JOptionPane.YES_NO_OPTION);
            if (c==JOptionPane.YES_OPTION) System.exit(0);
        });

        sideContent.add(btnPwd); sideContent.add(Box.createVerticalStrut(4));
        sideContent.add(btnLogout); sideContent.add(Box.createVerticalStrut(4));
        sideContent.add(btnExit);

        sidebar.add(sideContent,BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(sidebar,BorderLayout.WEST); add(cardPanel,BorderLayout.CENTER);
    }

    private JButton sideBtn(String text, Color fg) {
        JButton b=new JButton(text);
        b.setFont(new Font("Segoe UI",Font.PLAIN,13)); b.setForeground(fg);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setOpaque(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        return b;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(()->new LoginFrame().setVisible(true));
    }
}


