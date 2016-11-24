import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.regex.*;

public class UDP extends JFrame{
    private final String FRM_TITLE = "Our tiny chat";
    private final int FRM_LOC_X = 100;
    private final int FRM_LOC_Y = 100;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;
    //private final String IP_BROADCAST = "127.0.0.1";
    private final String IP_BROADCAST = "10.0.1.255"; // apple router
    private final int PORT = 9876;
    private JTextArea taMain;
    private JTextField tfMsg;

    private class thdReceiver extends Thread{
        @Override
        public void start(){
            super.start();
            try{
                customize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void customize() throws Exception{
            DatagramSocket receiverSocket = new DatagramSocket(PORT);
            Pattern regex = Pattern.compile("[\u0020-\uFFFF]");
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiverSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String sentence = new String(receivePacket.getData());
                Matcher m = regex.matcher(sentence);

                taMain.append(IPAddress.toString() + ":" + port + ": ");
                while(m.find())
                    taMain.append(sentence.substring(m.start(), m.end()));
                taMain.append("\r\n");
                taMain.setCaretPosition(taMain.getText().length());
            }
        }
    }

    private void btnSend_Handler() throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(IP_BROADCAST);
        byte[] sendData;
        String sentence = tfMsg.getText();
        tfMsg.setText("");
        sendData = sentence.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
        sendSocket.send(sendPacket);
   }

    private void frameDraw(JFrame frame){
        tfMsg = new JTextField();
        taMain = new JTextArea(FRM_HEIGHT/19, 50);
        JScrollPane spMain = new JScrollPane(taMain);

        spMain.setLocation(0, 0);
        taMain.setLineWrap(true);
        taMain.setEditable(false);

        JButton btnSend = new JButton();
        btnSend.setText("Send");
        btnSend.setToolTipText("Broadcast a message");
        btnSend.addActionListener(e -> {
            try {
                btnSend_Handler();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
        frame.setSize(FRM_WIDTH, FRM_HEIGHT);
        frame.setResizable(false);
        frame.getContentPane().add(BorderLayout.NORTH, spMain);
        frame.getContentPane().add(BorderLayout.CENTER, tfMsg);
        frame.getContentPane().add(BorderLayout.EAST, btnSend);
        frame.setVisible(true);
    }

    private void antiStatic(){
        frameDraw(new UDP()); //JFrame frame = new UDP(); frameDraw(frame);
        new thdReceiver().start();
    }

    public static void main(String[] args){
        new UDP().antiStatic();
    }
}
