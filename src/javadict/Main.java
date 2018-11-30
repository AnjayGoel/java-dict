package javadict;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Anjay
 */
//add tool tip to jlabel
//slides out unexpectely
class settings implements Serializable {

    int y;

    public settings() {
    }
}
//can get idioms too!

public final class Main extends javax.swing.JFrame {


    /**
     * Creates new form Main
     */
    Timer autoHideTimer;
    File save = new File("Settings");
    int y;
    boolean persistent = false;
    boolean visibleOnScreen = false;
    String previous_str = "";
     int current = 0;
    ArrayList<String> def_array = new ArrayList<>();
    String word = "";
            Toolkit tk = Toolkit.getDefaultToolkit();
    public Main() {
        
        //look and feel
        try {
            /* Set the Nimbus look and feel */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        setLocation();

        if (!visibleOnScreen) {
            slide(true);
        }
        //timer to update content
        Timer t = new Timer(20, null);
        t.setRepeats(true);

        t.addActionListener(new ActionListener() {
            @Override

            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        t.start();

    }

    public void setLocation() {
        int x = tk.getScreenSize().width+15;
        if (save.exists()) {
            try {
                FileInputStream in = new FileInputStream(save);
                ObjectInputStream i_in = new ObjectInputStream(in);
                settings s = (settings) i_in.readObject();

                setLocation(x, s.y);

                i_in.close();
                in.close();
            } catch (IOException | ClassNotFoundException ex) {
                System.out.println(ex +" "+ Thread.currentThread().getStackTrace()[1].getLineNumber());
            }
        } else {

            setLocation(x, tk.getScreenSize().height - (getHeight() + 40));

        }
          setVisible(true);
    }

   
    public void autoHide() {
        System.out.println("autohide called");
       autoHideTimer = new Timer(3000, null);
        autoHideTimer.setRepeats(false);
        autoHideTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("autohide calling slideout time over");
                slide(false);
                autoHideTimer.stop();
                 autoHideTimer = null;
            }
        });
        autoHideTimer.start();

    }
 
 static boolean testInet(String site) {
     //try to understand it
    Socket sock = new Socket();
    InetSocketAddress addr = new InetSocketAddress(site,80);
    try {
        sock.connect(addr,3000);
        return true;
    } catch (IOException e) {
        return false;
    } finally {
        try {sock.close();}
        catch (IOException e) {}
    }
}
    public void update() 
    {
        setAutoHide ();
        Clipboard clipboard = tk.getSystemClipboard();
        
        String clip_board = "";
        try {
            clip_board = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
         

        }
        if (!previous_str.equalsIgnoreCase(clip_board) && !clip_board.trim().equals("") && clip_board.length()<25 ) {
            if (!visibleOnScreen) {
                slide(true);
            }   
           
            label.setText("Please Wait While Fetching Data.......");

            if (autoHideTimer !=null)   autoHideTimer.restart();
            def_array.clear();
            previous_str = clip_board;
            clip_board = clip_board.toLowerCase().replaceAll("[^\\w]+", "").trim().replace("_", "");  
            given_word.setText(proper (clip_board));
      if ( !testInet("google.com")){
               label.setText("No Internet Connection Available");
               System.out.println("no internet available");
               return;
           }   
                if (!clip_board.equals("")) {
          
                String label_text =  getDef(clip_board);
                if (label_text.equals("")) {
                    label.setText("Sorry No Defination Found");
                } else {
                    label.setText(label_text);
                }
           
            }
        }
    }
public String proper (String s){
return  (s.charAt(0)+"").toUpperCase() + s.substring(1);
}

public void setAutoHide (){
float x1 = getX();
float y1 = getY();
float x2 = x1 + getWidth ();
float y2 = y1 + getHeight ();
float Mx = MouseInfo.getPointerInfo().getLocation().x;
float My = MouseInfo.getPointerInfo().getLocation().y;
if (Mx > x1 && Mx < x2 && My > y1 && My < y2 && autoHideTimer !=null){
Color def = new Color(255,255,255);
        exit.setForeground(def);
autoHideTimer.restart ();
}
else {

       Color def = new Color(20,126,255);
exit.setForeground(def); 
}
}

    public String getDef(String s) {

        String dict_data = "";
        try {
            URL get = new URL("https://glosbe.com/gapi/translate?from=en&dest=eng&format=json&phrase=" + s);
     
            URLConnection con = get.openConnection();
        
            con = (HttpURLConnection) con;
         
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String current_line;

            while ((current_line = in.readLine()) != null) {

                dict_data += current_line;
            }

        } catch (Exception ex) {
                        System.out.println(ex +" "+ Thread.currentThread().getStackTrace()[1].getLineNumber());
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (dict_data.contains("[")) {
            dict_data = getDefArray(dict_data);
        } else {
            dict_data = "";
        }
        return dict_data;

    }

    public  String getDefArray(String s) {
        long time = System.currentTimeMillis();

        int first_index = s.indexOf("[{") + 1;
        int last_index = s.indexOf("],");
        s = s.substring(first_index, last_index - 1);
        s = s.substring(s.indexOf("[") + 2);

        String arr[] = s.split("\\},\\{", 10);
        for (String temp : arr) {
            temp = temp.substring(temp.lastIndexOf(":") + 1);
            temp = temp.replaceAll("&#39;", "'");
            temp = temp.replaceAll("\"", " ");

            def_array.add(temp);
        }
        current = 0;
        return def_array.get(0);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        Panel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        exit = new javax.swing.JLabel();
        next = new javax.swing.JLabel();
        previous = new javax.swing.JLabel();
        given_word = new javax.swing.JLabel();
        keepit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusableWindowState(false);
        setUndecorated(true);

        Panel.setBackground(new java.awt.Color(20, 126, 255));
        Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 83, 208)));
        Panel.setToolTipText("");
        Panel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        Panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                PanelMouseDragged(evt);
            }
        });
        Panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                PanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                PanelMouseReleased(evt);
            }
        });

        label.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        label.setForeground(new java.awt.Color(242, 242, 242));
        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setText("Meaning");

        exit.setBackground(new java.awt.Color(20, 126, 255));
        exit.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        exit.setForeground(new java.awt.Color(20, 126, 255));
        exit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        exit.setText("x");
        exit.setToolTipText("");
        exit.setOpaque(true);
        exit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitMouseClicked(evt);
            }
        });

        next.setForeground(new java.awt.Color(242, 242, 242));
        next.setText("Next");
        next.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nextMouseClicked(evt);
            }
        });

        previous.setForeground(new java.awt.Color(242, 242, 242));
        previous.setText("Previous");
        previous.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                previousMouseClicked(evt);
            }
        });

        given_word.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        given_word.setForeground(new java.awt.Color(239, 239, 239));
        given_word.setText("Word");

        keepit.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        keepit.setForeground(new java.awt.Color(255, 255, 255));
        keepit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        keepit.setText("Keep Window");
        keepit.setToolTipText("");
        keepit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                keepitMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout PanelLayout = new javax.swing.GroupLayout(Panel);
        Panel.setLayout(PanelLayout);
        PanelLayout.setHorizontalGroup(
            PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelLayout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addComponent(previous)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(keepit)
                .addGap(18, 18, 18)
                .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(69, 69, 69))
            .addGroup(PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelLayout.createSequentialGroup()
                        .addComponent(given_word, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelLayout.createSequentialGroup()
                        .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(37, Short.MAX_VALUE))))
        );
        PanelLayout.setVerticalGroup(
            PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelLayout.createSequentialGroup()
                .addGroup(PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exit)
                    .addGroup(PanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(given_word, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previous)
                    .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keepit))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>                        

    private void nextMouseClicked(java.awt.event.MouseEvent evt) {                                  
        // TODO add your handling code here:
        if (def_array.isEmpty()) {
            return;
        }
        if (current + 1 < def_array.size()) {
            current++;
        }

        label.setText(def_array.get(current));
    }                                 

    private void previousMouseClicked(java.awt.event.MouseEvent evt) {                                      
        // TODO add your handling code here:
        if (def_array.isEmpty()) {
            return;
        }
        if (current != 0) {
            current--;
        }
        label.setText(def_array.get(current));
    }                                     

    private void PanelMouseDragged(java.awt.event.MouseEvent evt) {                                   
        // TODO add your handling code here:
      setLocation(getLocation().x, getLocation().y + evt.getY() - y);
      
    }                                  

    private void PanelMousePressed(java.awt.event.MouseEvent evt) {                                   
        // TODO add your handling code here:
            y = evt.getY();
   
    }                                  

    private void PanelMouseReleased(java.awt.event.MouseEvent evt) {                                    
        // TODO add your handling code here:
        
        
            try {
            FileOutputStream out = new FileOutputStream(save);
            ObjectOutputStream o_out = new ObjectOutputStream(out);
            settings s = new settings();

            s.y = getY();
            o_out.writeObject(s);
            o_out.close();
            out.close();
        } catch (Exception ex) {
            //do something
                    System.out.println(ex +" "+ Thread.currentThread().getStackTrace()[1].getLineNumber());
        }
                                      

        
        
    }                                   

    private void keepitMouseClicked(java.awt.event.MouseEvent evt) {                                    
        // TODO add your handling code here:
        if (!persistent)   {
            autoHideTimer.stop();
            autoHideTimer = null;
        keepit.setText("Auto Hide It");
        persistent = true;
        }
        else {
     if ( autoHideTimer == null) autoHide();
    
        autoHideTimer.start();
        keepit.setText("Keep Window");
        persistent = false;
       
        }
         System.out.println("is persistent"+ persistent);
    }                                   

    private void exitMouseClicked(java.awt.event.MouseEvent evt) {                                  
        // TODO add your handling code here:
        System.exit(0);
    }                                 

    public void slide(boolean x) {
        System.out.println("slide called with" + x);
        visibleOnScreen = x;
        final Timer timer = new Timer(10, null);
        timer.setRepeats(true);
        timer.addActionListener(new ActionListener() {
            int length = 0;
            int limit = getWidth();

            @Override

            public void actionPerformed(ActionEvent e) {
                if (length < limit) {
                    if (x) {
                        slideIn();
                    } else {
                        slideOut();

                    }
                    length += 15;
                } else {

                    fixPos(x);
                    if (x) {
                        autoHide();
                    }

                    //problem here calls even if false
                    timer.stop();
                }
            }
        }
        );

        timer.start();

    }

    public void slideIn() {

        this.setLocation(this.getX() - 15, this.getY());

    }

    public void slideOut() {

        this.setLocation(this.getX() + 15, this.getY());

    }

    public void fixPos(boolean type) {
        int xPos;
        if (type) {
            xPos = tk.getScreenSize().width - (getWidth());
        } else {
            xPos = tk.getScreenSize().width;
        }

        this.setLocation(xPos, this.getY());
        System.out.println("visible on screen "+ type);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
      
                new Main();
    }

    // Variables declaration - do not modify                     
    private javax.swing.JPanel Panel;
    private javax.swing.JLabel exit;
    private javax.swing.JLabel given_word;
    private javax.swing.JLabel keepit;
    public javax.swing.JLabel label;
    private javax.swing.JLabel next;
    private javax.swing.JLabel previous;
    // End of variables declaration                   
}
