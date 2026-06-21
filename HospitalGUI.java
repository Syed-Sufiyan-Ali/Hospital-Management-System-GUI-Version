import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// ===================== GRADIENT PANEL =====================
class GradientPanel extends JPanel {
    private Color c1, c2;
    GradientPanel(Color c1, Color c2) { this.c1=c1; this.c2=c2; setOpaque(false); }
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

// ===================== MODERN STYLED BUTTON =====================
class ModernButton extends JButton {
    private Color base, hover, pressed;
    private boolean isHovered = false, isPressed = false;

    ModernButton(String text, Color base) {
        super(text);
        this.base = base;
        this.hover = base.brighter();
        this.pressed = base.darker();
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { isHovered=true; repaint(); }
            public void mouseExited(MouseEvent e)  { isHovered=false; repaint(); }
            public void mousePressed(MouseEvent e) { isPressed=true; repaint(); }
            public void mouseReleased(MouseEvent e){ isPressed=false; repaint(); }
        });
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color draw = isPressed ? pressed : (isHovered ? hover : base);
        g2.setColor(draw);
        g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
        // subtle top highlight
        g2.setColor(new Color(255,255,255,40));
        g2.fillRoundRect(0,0,getWidth(),getHeight()/2,12,12);
        g2.dispose();
        super.paintComponent(g);
    }
}

// ===================== MODERN TEXT FIELD =====================
class ModernField extends JTextField {
    private String placeholder;
    ModernField(String placeholder) {
        this.placeholder = placeholder;
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setForeground(new Color(30,30,30));
        setBackground(new Color(250,251,255));
        setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(200,210,230), 8),
            BorderFactory.createEmptyBorder(8,12,8,12)));
        setOpaque(true);
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(170,180,200));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            Insets ins = getInsets();
            g2.drawString(placeholder, ins.left, getHeight()-ins.bottom-4);
        }
    }
}

// ===================== ROUND BORDER =====================
class RoundBorder extends AbstractBorder {
    private Color color; private int radius;
    RoundBorder(Color c, int r) { color=c; radius=r; }
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color); g2.drawRoundRect(x,y,w-1,h-1,radius,radius);
        g2.dispose();
    }
    public Insets getBorderInsets(Component c) { return new Insets(radius/2,radius/2,radius/2,radius/2); }
    public Insets getBorderInsets(Component c, Insets in) { in.set(radius/2,radius/2,radius/2,radius/2); return in; }
}

// ===================== BASE PANEL =====================
abstract class BasePanel extends JPanel {
    protected static String[][] patients = new String[100][5];
    protected static int totalPatients = 0;
    protected static final String FILE = "patients.txt";

    // Design constants
    static final Color BG        = new Color(240, 244, 255);
    static final Color CARD_BG   = Color.WHITE;
    static final Color ACCENT    = new Color(79, 70, 229);
    static final Color ACCENT2   = new Color(16, 185, 129);
    static final Color DANGER    = new Color(239, 68, 68);
    static final Color WARNING   = new Color(245, 158, 11);
    static final Color TEXT_DARK = new Color(17, 24, 39);
    static final Color TEXT_MID  = new Color(75, 85, 99);
    static final Color TEXT_LITE = new Color(156, 163, 175);

    BasePanel() { setBackground(BG); }

    protected void loadPatients() {
        totalPatients = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null && totalPatients < 100) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) patients[totalPatients++] = parts;
            }
        } catch (IOException e) {}
    }

    protected void savePatients() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE))) {
            for (int i = 0; i < totalPatients; i++) {
                bw.write(String.join("|", patients[i])); bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"Error saving!","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    protected JLabel makeHeading(String title, String subtitle) {
        JLabel lbl = new JLabel("<html><span style='font-size:18px;font-weight:bold;color:#111827'>"
            + title + "</span><br><span style='font-size:11px;color:#6B7280'>" + subtitle + "</span></html>");
        return lbl;
    }

    protected RoundedPanel makeCard() {
        RoundedPanel p = new RoundedPanel(16, CARD_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(makeHeading("Patient Records","View and manage all registered patients"), BorderLayout.WEST);
        ModernButton btnRefresh = new ModernButton("↻  Refresh", ACCENT);
        btnRefresh.setPreferredSize(new Dimension(120,36));
        btnRefresh.addActionListener(e -> refresh());
        topBar.add(btnRefresh, BorderLayout.EAST);

        String[] cols = {"Patient Name","Age","Gender","Patient ID","Diagnosis"};
        model = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){ return false; } };
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0,4));
        table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(238,240,255));
        table.setSelectionForeground(TEXT_DARK);
        table.setFocusable(false);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(new Font("Segoe UI",Font.BOLD,12));
        hdr.setBackground(new Color(249,250,255));
        hdr.setForeground(new Color(99,102,241));
        hdr.setPreferredSize(new Dimension(0,42));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,2,0,new Color(224,226,255)));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (!sel) {
                    setBackground(row%2==0 ? CARD_BG : new Color(248,249,255));
                    setForeground(TEXT_DARK);
                }
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new RoundBorder(new Color(224,226,255),12));
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);

        statLabel = new JLabel("  Loading...");
        statLabel.setFont(new Font("Segoe UI",Font.PLAIN,12));
        statLabel.setForeground(TEXT_LITE);

        add(topBar, BorderLayout.NORTH);
        add(sp,     BorderLayout.CENTER);
        add(statLabel, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        loadPatients();
        model.setRowCount(0);
        for (int i=0;i<totalPatients;i++) model.addRow(patients[i]);
        statLabel.setText("  " + totalPatients + " patient" + (totalPatients!=1?"s":"") + " registered");
    }
}

// ===================== ADD PANEL =====================
class AddPanel extends BasePanel {
    private ModernField fName,fAge,fGender,fId,fIllness;
    private JLabel status;

    AddPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        add(makeHeading("Add New Patient","Register a new patient into the system"), BorderLayout.NORTH);

        RoundedPanel card = makeCard();
        card.setLayout(new BorderLayout(0,20));

        JPanel form = new JPanel(new GridLayout(5,2,16,14));
        form.setOpaque(false);

        fName    = new ModernField("e.g. Ahmed Khan");
        fAge     = new ModernField("e.g. 35");
        fGender  = new ModernField("M or F");
        fId      = new ModernField("e.g. P-001");
        fIllness = new ModernField("e.g. Hypertension");

        Font lf = new Font("Segoe UI",Font.BOLD,13);
        String[] lbls = {"Full Name","Age","Gender (M/F)","Patient ID","Diagnosis / Illness"};
        ModernField[] fields = {fName,fAge,fGender,fId,fIllness};
        for (int i=0;i<lbls.length;i++) {
            JLabel l = new JLabel(lbls[i]); l.setFont(lf); l.setForeground(TEXT_MID);
            form.add(l); form.add(fields[i]);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        btnRow.setOpaque(false);
        ModernButton btnAdd   = new ModernButton("＋  Add Patient",ACCENT2);
        ModernButton btnClear = new ModernButton("↺  Clear",  new Color(107,114,128));
        btnAdd.setPreferredSize(new Dimension(150,38));
        btnClear.setPreferredSize(new Dimension(120,38));
        btnAdd.addActionListener(e -> addPatient());
        btnClear.addActionListener(e -> clearFields());
        btnRow.add(btnAdd); btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(btnClear);

        status = new JLabel(" ");
        status.setFont(new Font("Segoe UI",Font.PLAIN,13));

        JPanel bottom = new JPanel(new BorderLayout(0,8));
        bottom.setOpaque(false);
        bottom.add(btnRow,  BorderLayout.NORTH);
        bottom.add(status,  BorderLayout.SOUTH);

        card.add(form,   BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void addPatient() {
        loadPatients();
        if (totalPatients >= 100) { setStatus("System full!",DANGER); return; }
        String name=fName.getText().trim(), age=fAge.getText().trim();
        String gender=fGender.getText().trim().toUpperCase();
        String id=fId.getText().trim(), illness=fIllness.getText().trim();
        if (name.isEmpty()||age.isEmpty()||gender.isEmpty()||id.isEmpty()||illness.isEmpty()) {
            setStatus("All fields are required!",DANGER); return;
        }
        for (int i=0;i<totalPatients;i++) {
            if (patients[i][3].equalsIgnoreCase(id)) { setStatus("Patient ID already exists!",DANGER); return; }
        }
        patients[totalPatients++] = new String[]{name,age,gender,id,illness};
        savePatients();
        setStatus("✓  Patient registered successfully!",ACCENT2);
        clearFields();
    }

    private void clearFields() {
        fName.setText(""); fAge.setText(""); fGender.setText(""); fId.setText(""); fIllness.setText("");
    }

    private void setStatus(String msg, Color c) { status.setText("  "+msg); status.setForeground(c); }
    public void refresh() {}
}

// ===================== SEARCH PANEL =====================
class SearchPanel extends BasePanel {
    private DefaultTableModel model;
    private JLabel status;

    SearchPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Search Patient","Find any patient by their unique ID"), BorderLayout.NORTH);

        RoundedPanel card = makeCard();
        card.setLayout(new BorderLayout(0,16));

        ModernField fId = new ModernField("Enter Patient ID...");
        ModernButton btnSearch = new ModernButton("🔍  Search", ACCENT);
        btnSearch.setPreferredSize(new Dimension(130,38));

        JPanel searchBar = new JPanel(new BorderLayout(10,0));
        searchBar.setOpaque(false);
        JLabel lbl = new JLabel("Patient ID"); lbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        lbl.setForeground(TEXT_MID); lbl.setPreferredSize(new Dimension(90,0));
        searchBar.add(lbl,       BorderLayout.WEST);
        searchBar.add(fId,       BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        String[] cols = {"Patient Name","Age","Gender","Patient ID","Diagnosis"};
        model = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){ return false; } };
        JTable table = new JTable(model);
        table.setRowHeight(40); table.setFont(new Font("Segoe UI",Font.PLAIN,13));
        table.setShowVerticalLines(false); table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(238,240,255));
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.getTableHeader().setBackground(new Color(249,250,255));
        table.getTableHeader().setForeground(new Color(99,102,241));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){ setBackground(CARD_BG); setForeground(TEXT_DARK); }
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12)); return this;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new RoundBorder(new Color(224,226,255),12));
        sp.getViewport().setBackground(CARD_BG);

        status = new JLabel("  Enter a Patient ID and press Search");
        status.setFont(new Font("Segoe UI",Font.ITALIC,12));
        status.setForeground(TEXT_LITE);

        ActionListener doSearch = e -> {
            String id=fId.getText().trim();
            if (id.isEmpty()){ status.setText("  Please enter a Patient ID"); return; }
            loadPatients(); model.setRowCount(0); boolean found=false;
            for (int i=0;i<totalPatients;i++)
                if (patients[i][3].equalsIgnoreCase(id)){ model.addRow(patients[i]); found=true; break; }
            status.setText(found ? "  ✓ Patient found!" : "  ✗ No patient found with ID: "+id);
            status.setForeground(found ? ACCENT2 : DANGER);
        };
        btnSearch.addActionListener(doSearch);
        fId.addActionListener(doSearch);

        card.add(searchBar, BorderLayout.NORTH);
        card.add(sp,        BorderLayout.CENTER);
        card.add(status,    BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }
    public void refresh() {}
}

// ===================== DELETE PANEL =====================
class DeletePanel extends BasePanel {
    private JLabel status;

    DeletePanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Delete Patient","Permanently remove a patient record"), BorderLayout.NORTH);

        RoundedPanel card = makeCard();
        card.setLayout(new BorderLayout(0,16));

        ModernField fId = new ModernField("Enter Patient ID to delete...");
        ModernButton btnDel = new ModernButton("🗑  Delete Patient", DANGER);
        btnDel.setPreferredSize(new Dimension(160,38));

        JPanel row = new JPanel(new BorderLayout(10,0));
        row.setOpaque(false);
        JLabel lbl = new JLabel("Patient ID"); lbl.setFont(new Font("Segoe UI",Font.BOLD,13));
        lbl.setForeground(TEXT_MID); lbl.setPreferredSize(new Dimension(90,0));
        row.add(lbl, BorderLayout.WEST);
        row.add(fId, BorderLayout.CENTER);
        row.add(btnDel, BorderLayout.EAST);

        // Warning box
        RoundedPanel warn = new RoundedPanel(10, new Color(255,247,237));
        warn.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(253,186,116),10),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        warn.setLayout(new BorderLayout());
        JLabel warnText = new JLabel("<html>⚠️ &nbsp;<b>Warning:</b> This action is permanent and cannot be undone. "
            + "Please verify the Patient ID before proceeding.</html>");
        warnText.setFont(new Font("Segoe UI",Font.PLAIN,12));
        warnText.setForeground(new Color(146,64,14));
        warn.add(warnText);

        status = new JLabel("  Enter a Patient ID above to delete");
        status.setFont(new Font("Segoe UI",Font.PLAIN,13));
        status.setForeground(TEXT_LITE);

        btnDel.addActionListener(e -> {
            String id=fId.getText().trim();
            if (id.isEmpty()){ status.setText("  Enter a Patient ID first!"); status.setForeground(DANGER); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete patient with ID: " + id + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm!=JOptionPane.YES_OPTION) return;
            loadPatients(); boolean found=false;
            for (int i=0;i<totalPatients;i++) {
                if (patients[i][3].equalsIgnoreCase(id)) {
                    for (int j=i;j<totalPatients-1;j++) patients[j]=patients[j+1];
                    totalPatients--; found=true; break;
                }
            }
            if (found){ savePatients(); status.setText("  ✓ Patient deleted successfully!"); status.setForeground(ACCENT2); fId.setText(""); }
            else      { status.setText("  ✗ No patient found with ID: "+id); status.setForeground(DANGER); }
        });

        card.add(row,    BorderLayout.NORTH);
        card.add(warn,   BorderLayout.CENTER);
        card.add(status, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }
    public void refresh() {}
}

// ===================== INVOICE DIALOG =====================
class InvoiceDialog extends JDialog {
    InvoiceDialog(Frame owner, String patientName, String patientId, String illness,
                  String age, String gender, int days, int ratePerDay) {
        super(owner, "Invoice Receipt", true);
        setSize(520, 720);
        setLocationRelativeTo(owner);
        setResizable(false);

        int roomCharge  = days * ratePerDay;
        int docFee      = days * 500;
        int nursingFee  = days * 300;
        int medCharges  = days * 200;
        int subtotal    = roomCharge + docFee + nursingFee + medCharges;
        int tax         = (int)(subtotal * 0.05);
        int total       = subtotal + tax;

        String invoiceNo  = "INV-" + System.currentTimeMillis() % 100000;
        String date       = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ---- HEADER ----
        GradientPanel header = new GradientPanel(new Color(79,70,229), new Color(99,102,241));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        header.setPreferredSize(new Dimension(0,130));

        JPanel hLeft = new JPanel(new GridLayout(4,1,0,2));
        hLeft.setOpaque(false);
        JLabel hospName = new JLabel("MedCare Hospital");
        hospName.setFont(new Font("Segoe UI",Font.BOLD,20)); hospName.setForeground(Color.WHITE);
        JLabel hospSub = new JLabel("Advanced Healthcare Services");
        hospSub.setFont(new Font("Segoe UI",Font.PLAIN,11)); hospSub.setForeground(new Color(199,210,254));
        JLabel invNo = new JLabel("Invoice # " + invoiceNo);
        invNo.setFont(new Font("Segoe UI",Font.BOLD,12)); invNo.setForeground(new Color(224,231,255));
        JLabel invDate = new JLabel(date);
        invDate.setFont(new Font("Segoe UI",Font.PLAIN,11)); invDate.setForeground(new Color(199,210,254));
        hLeft.add(hospName); hLeft.add(hospSub); hLeft.add(invNo); hLeft.add(invDate);

        JLabel paidBadge = new JLabel("RECEIPT", SwingConstants.CENTER);
        paidBadge.setFont(new Font("Segoe UI",Font.BOLD,13));
        paidBadge.setForeground(new Color(79,70,229));
        paidBadge.setBackground(Color.WHITE);
        paidBadge.setOpaque(true);
        paidBadge.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        paidBadge.setPreferredSize(new Dimension(90,36));

        header.add(hLeft,     BorderLayout.WEST);
        header.add(paidBadge, BorderLayout.EAST);

        // ---- BODY ----
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20,28,20,28));

        // Patient info section
        body.add(sectionTitle("PATIENT INFORMATION"));
        body.add(Box.createVerticalStrut(8));
        JPanel patInfo = new JPanel(new GridLayout(2,4,8,6));
        patInfo.setBackground(new Color(249,250,255));
        patInfo.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(224,226,255),8),
            BorderFactory.createEmptyBorder(12,14,12,14)));
        patInfo.add(infoItem("Name",  patientName));
        patInfo.add(infoItem("ID",    patientId));
        patInfo.add(infoItem("Age",   age + " yrs"));
        patInfo.add(infoItem("Gender",gender.equals("M")?"Male":gender.equals("F")?"Female":gender));
        patInfo.add(infoItem("Diagnosis", illness));
        patInfo.add(infoItem("Admission","General Ward"));
        patInfo.add(infoItem("Days Admitted", days + (days==1?" day":" days")));
        patInfo.add(infoItem("Status","Discharged"));
        body.add(patInfo);

        // Billing breakdown
        body.add(Box.createVerticalStrut(18));
        body.add(sectionTitle("BILLING BREAKDOWN"));
        body.add(Box.createVerticalStrut(8));

        JPanel billTable = new JPanel(new GridBagLayout());
        billTable.setBackground(Color.WHITE);
        billTable.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(229,231,235),10),
            BorderFactory.createEmptyBorder(0,0,0,0)));

        addBillHeader(billTable);
        addBillRow(billTable, 0, "Room & Board",    "Per day × "+days, ratePerDay, roomCharge, false);
        addBillRow(billTable, 1, "Doctor's Fee",    "Per day × "+days, 500, docFee, false);
        addBillRow(billTable, 2, "Nursing Care",    "Per day × "+days, 300, nursingFee, false);
        addBillRow(billTable, 3, "Medications",     "Per day × "+days, 200, medCharges, true);
        body.add(billTable);

        // Totals
        body.add(Box.createVerticalStrut(10));
        JPanel totals = new JPanel(new BorderLayout());
        totals.setBackground(Color.WHITE);
        JPanel totInner = new JPanel(new GridLayout(3,2,0,4));
        totInner.setOpaque(false);
        totInner.setBorder(BorderFactory.createEmptyBorder(0,200,0,0));
        addTotalRow(totInner, "Subtotal",     "Rs. "+subtotal, false);
        addTotalRow(totInner, "Tax (5%)",     "Rs. "+tax,      false);
        addTotalRow(totInner, "TOTAL DUE",    "Rs. "+total,    true);
        totals.add(totInner, BorderLayout.EAST);
        body.add(totals);

        // Footer note
        body.add(Box.createVerticalStrut(16));
        JLabel footer = new JLabel("<html><center><i>Thank you for choosing MedCare Hospital.<br>"
            + "For billing inquiries call: <b>021-111-MEDCARE</b></i></center></html>",
            SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI",Font.PLAIN,11));
        footer.setForeground(new Color(156,163,175));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(footer);

        // ---- ACTIONS ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,10));
        actions.setBackground(new Color(249,250,255));
        actions.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));

        ModernButton btnPrint = new ModernButton("🖨  Print",  new Color(79,70,229));
        ModernButton btnClose = new ModernButton("✕  Close",  new Color(107,114,128));
        btnPrint.setPreferredSize(new Dimension(110,36));
        btnClose.setPreferredSize(new Dimension(110,36));

        btnPrint.addActionListener(e -> printInvoice(body));
        btnClose.addActionListener(e -> dispose());

        actions.add(btnPrint);
        actions.add(btnClose);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        root.add(header,  BorderLayout.NORTH);
        root.add(scroll,  BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel infoItem(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2,1,0,2)); p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI",Font.PLAIN,10)); lbl.setForeground(new Color(156,163,175));
        JLabel val = new JLabel(value); val.setFont(new Font("Segoe UI",Font.BOLD,12));  val.setForeground(new Color(17,24,39));
        p.add(lbl); p.add(val); return p;
    }

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,10));
        l.setForeground(new Color(99,102,241));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void addBillHeader(JPanel p) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.gridy=0;
        String[] headers = {"Description","Rate","Qty","Amount"};
        int[] widths = {180,80,100,100};
        for (int i=0;i<headers.length;i++) {
            JLabel l = new JLabel(headers[i]);
            l.setFont(new Font("Segoe UI",Font.BOLD,11));
            l.setForeground(new Color(99,102,241));
            l.setBackground(new Color(238,240,255));
            l.setOpaque(true);
            l.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
            l.setHorizontalAlignment(i>0?SwingConstants.RIGHT:SwingConstants.LEFT);
            gc.gridx=i; gc.weightx=(i==0)?1:0; gc.ipadx=widths[i]-40;
            p.add(l,gc);
        }
    }

    private void addBillRow(JPanel p, int row, String desc, String qty, int rate, int amount, boolean lastItem) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.gridy=row+1;
        Color bg = row%2==0 ? Color.WHITE : new Color(249,250,255);
        Border bottom = lastItem ? BorderFactory.createEmptyBorder() :
            BorderFactory.createMatteBorder(0,0,1,0,new Color(243,244,246));

        String[] vals = {desc, "Rs."+rate, qty, "Rs."+amount};
        for (int i=0;i<vals.length;i++) {
            JLabel l = new JLabel(vals[i]);
            l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(new Color(55,65,81));
            l.setBackground(bg); l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(bottom, BorderFactory.createEmptyBorder(10,12,10,12)));
            l.setHorizontalAlignment(i>0?SwingConstants.RIGHT:SwingConstants.LEFT);
            gc.gridx=i; gc.weightx=(i==0)?1:0;
            p.add(l,gc);
        }
    }

    private void addTotalRow(JPanel p, String label, String amount, boolean bold) {
        JLabel l = new JLabel(label);
        JLabel a = new JLabel(amount);
        Font f = bold ? new Font("Segoe UI",Font.BOLD,14) : new Font("Segoe UI",Font.PLAIN,12);
        Color c = bold ? new Color(79,70,229) : new Color(55,65,81);
        l.setFont(f); l.setForeground(c);
        a.setFont(f); a.setForeground(c);
        a.setHorizontalAlignment(SwingConstants.RIGHT);
        if (bold) {
            l.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));
            a.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(229,231,235)));
        }
        p.add(l); p.add(a);
    }

    private void printInvoice(JPanel panel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pf, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double scaleX = pf.getImageableWidth()  / panel.getWidth();
            double scaleY = pf.getImageableHeight() / panel.getHeight();
            double scale  = Math.min(scaleX, scaleY);
            g2.scale(scale, scale);
            panel.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,"Print error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// ===================== BILL PANEL =====================
class BillPanel extends BasePanel {
    private ModernField fId, fDays;
    private JComboBox<String> roomType;
    private JLabel status;

    BillPanel() {
        setLayout(new BorderLayout(0,16));
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        add(makeHeading("Billing & Invoice","Generate detailed invoices for patient stays"), BorderLayout.NORTH);

        RoundedPanel card = makeCard();
        card.setLayout(new BorderLayout(0,20));

        JPanel form = new JPanel(new GridLayout(3,2,16,14));
        form.setOpaque(false);

        fId   = new ModernField("Enter Patient ID...");
        fDays = new ModernField("Number of days...");
        roomType = new JComboBox<>(new String[]{"General Ward  — Rs.1,000/day","Semi-Private — Rs.2,000/day","Private Room — Rs.3,500/day","ICU           — Rs.6,000/day"});
        roomType.setFont(new Font("Segoe UI",Font.PLAIN,13));
        roomType.setBackground(new Color(250,251,255));

        Font lf = new Font("Segoe UI",Font.BOLD,13);
        JLabel l1=new JLabel("Patient ID"),l2=new JLabel("Days Admitted"),l3=new JLabel("Room Type");
        for (JLabel l:new JLabel[]{l1,l2,l3}){ l.setFont(lf); l.setForeground(TEXT_MID); }
        form.add(l1); form.add(fId);
        form.add(l2); form.add(fDays);
        form.add(l3); form.add(roomType);

        ModernButton btnGenerate = new ModernButton("📄  Generate Invoice", new Color(79,70,229));
        btnGenerate.setPreferredSize(new Dimension(200,42));

        status = new JLabel("  Fill in the fields above and generate an invoice");
        status.setFont(new Font("Segoe UI",Font.ITALIC,12));
        status.setForeground(TEXT_LITE);

        // Info cards row
        JPanel infoRow = new JPanel(new GridLayout(1,4,12,0));
        infoRow.setOpaque(false);
        String[][] infos = {{"1,000","General Ward /day"},{"2,000","Semi-Private /day"},{"3,500","Private Room /day"},{"6,000","ICU /day"}};
        Color[] infoColors = {new Color(79,70,229),new Color(16,185,129),new Color(245,158,11),new Color(239,68,68)};
        for (int i=0;i<infos.length;i++) {
            RoundedPanel ic = new RoundedPanel(12, infoColors[i]);
            ic.setLayout(new GridLayout(2,1,0,4));
            ic.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
            JLabel amt = new JLabel("Rs. "+infos[i][0]); amt.setFont(new Font("Segoe UI",Font.BOLD,16)); amt.setForeground(Color.WHITE);
            JLabel sub = new JLabel(infos[i][1]);         sub.setFont(new Font("Segoe UI",Font.PLAIN,11)); sub.setForeground(new Color(255,255,255,200));
            ic.add(amt); ic.add(sub);
            infoRow.add(ic);
        }

        btnGenerate.addActionListener(e -> {
            String id=fId.getText().trim(), dStr=fDays.getText().trim();
            if (id.isEmpty()||dStr.isEmpty()){ setStatus("All fields are required!",DANGER); return; }
            try {
                int days = Integer.parseInt(dStr);
                if (days<=0){ setStatus("Days must be greater than 0!",DANGER); return; }
                loadPatients();
                String[] found = null;
                for (int i=0;i<totalPatients;i++) if (patients[i][3].equalsIgnoreCase(id)){ found=patients[i]; break; }
                if (found==null){ setStatus("✗ Patient not found with ID: "+id, DANGER); return; }
                int[] rates = {1000,2000,3500,6000};
                int rate = rates[roomType.getSelectedIndex()];
                InvoiceDialog dlg = new InvoiceDialog(
                    (Frame)SwingUtilities.getWindowAncestor(this),
                    found[0], found[3], found[4], found[1], found[2], days, rate);
                dlg.setVisible(true);
                setStatus("✓ Invoice generated for "+found[0], ACCENT2);
            } catch (NumberFormatException ex) { setStatus("Enter a valid number for days!",DANGER); }
        });

        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        btnArea.setOpaque(false);
        btnArea.add(btnGenerate);

        JPanel bottom = new JPanel(new BorderLayout(0,8));
        bottom.setOpaque(false);
        bottom.add(btnArea, BorderLayout.NORTH);
        bottom.add(status,  BorderLayout.SOUTH);

        card.add(form,    BorderLayout.NORTH);
        card.add(infoRow, BorderLayout.CENTER);
        card.add(bottom,  BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void setStatus(String msg, Color c){ status.setText("  "+msg); status.setForeground(c); }
    public void refresh() {}
}

// ===================== MAIN APP =====================
public class HospitalGUI extends JFrame {

    HospitalGUI() {
        setTitle("MedCare — Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 560));

        // ---- SIDEBAR ----
        GradientPanel sidebar = new GradientPanel(new Color(30,27,75), new Color(49,46,129));
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(210, 0));

        JPanel sideContent = new JPanel();
        sideContent.setLayout(new BoxLayout(sideContent, BoxLayout.Y_AXIS));
        sideContent.setOpaque(false);
        sideContent.setBorder(BorderFactory.createEmptyBorder(28,14,20,14));

        // Logo area
        JPanel logoArea = new JPanel(new BorderLayout(0,4));
        logoArea.setOpaque(false);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel cross = new JLabel("✚", SwingConstants.CENTER);
        cross.setFont(new Font("Segoe UI",Font.BOLD,28)); cross.setForeground(new Color(165,180,252));
        JLabel hospLabel = new JLabel("MedCare HMS", SwingConstants.CENTER);
        hospLabel.setFont(new Font("Segoe UI",Font.BOLD,14)); hospLabel.setForeground(Color.WHITE);
        JLabel hospSub = new JLabel("Management System", SwingConstants.CENTER);
        hospSub.setFont(new Font("Segoe UI",Font.PLAIN,10)); hospSub.setForeground(new Color(148,163,184));
        logoArea.add(cross,     BorderLayout.NORTH);
        logoArea.add(hospLabel, BorderLayout.CENTER);
        logoArea.add(hospSub,   BorderLayout.SOUTH);
        sideContent.add(logoArea);
        sideContent.add(Box.createVerticalStrut(28));

        // Separator
        JSeparator sep = new JSeparator(); sep.setForeground(new Color(255,255,255,30));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        sideContent.add(sep);
        sideContent.add(Box.createVerticalStrut(16));

        // Section label
        JLabel navLabel = new JLabel("  NAVIGATION");
        navLabel.setFont(new Font("Segoe UI",Font.BOLD,10));
        navLabel.setForeground(new Color(148,163,184));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideContent.add(navLabel);
        sideContent.add(Box.createVerticalStrut(8));

        // Card panel
        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        cardPanel.setBackground(BasePanel.BG);

        ViewPanel   viewPanel   = new ViewPanel();
        AddPanel    addPanel    = new AddPanel();
        SearchPanel searchPanel = new SearchPanel();
        BillPanel   billPanel   = new BillPanel();
        DeletePanel deletePanel = new DeletePanel();

        cardPanel.add(viewPanel,   "view");
        cardPanel.add(addPanel,    "add");
        cardPanel.add(searchPanel, "search");
        cardPanel.add(billPanel,   "bill");
        cardPanel.add(deletePanel, "delete");

        // Nav items: [label, key, icon, active bg]
        String[][] nav = {
            {"View Patients",  "view",   "👥"},
            {"Add Patient",    "add",    "➕"},
            {"Search Patient", "search", "🔍"},
            {"Generate Bill",  "bill",   "📄"},
            {"Delete Patient", "delete", "🗑"},
        };

        ButtonGroup group = new ButtonGroup();
        JToggleButton[] navBtns = new JToggleButton[nav.length];

        for (int i=0;i<nav.length;i++) {
            final String key = nav[i][1];
            JToggleButton btn = new JToggleButton(nav[i][2] + "  " + nav[i][0]) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected()) {
                        g2.setColor(new Color(255,255,255,25));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                        g2.setColor(new Color(165,180,252));
                        g2.fillRoundRect(0,(getHeight()-20)/2,3,20,3,3);
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(255,255,255,12));
                        g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
            btn.setForeground(isFirstBtn(i) ? Color.WHITE : new Color(148,163,184));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));

            final int idx = i;
            btn.addActionListener(e -> {
                cards.show(cardPanel, key);
                for (JToggleButton b : navBtns)
                    b.setForeground(b.isSelected() ? Color.WHITE : new Color(148,163,184));
            });
            group.add(btn);
            navBtns[i] = btn;
            sideContent.add(btn);
            sideContent.add(Box.createVerticalStrut(4));
        }
        navBtns[0].setSelected(true);

        sideContent.add(Box.createVerticalGlue());
        JSeparator sep2 = new JSeparator(); sep2.setForeground(new Color(255,255,255,30));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        sideContent.add(sep2);
        sideContent.add(Box.createVerticalStrut(10));

        // Exit button
        JButton btnExit = new JButton("⏻  Exit System");
        btnExit.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btnExit.setForeground(new Color(252,165,165));
        btnExit.setHorizontalAlignment(SwingConstants.LEFT);
        btnExit.setFocusPainted(false); btnExit.setBorderPainted(false); btnExit.setContentAreaFilled(false);
        btnExit.setOpaque(false); btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExit.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        btnExit.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,"Exit MedCare HMS?","Exit",JOptionPane.YES_NO_OPTION);
            if (c==JOptionPane.YES_OPTION) System.exit(0);
        });
        sideContent.add(btnExit);

        sidebar.add(sideContent, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(sidebar,   BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }

    private boolean isFirstBtn(int i) { return i==0; }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new HospitalGUI().setVisible(true));
    }
}