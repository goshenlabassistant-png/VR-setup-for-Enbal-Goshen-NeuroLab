package mainFlow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import com.fazecast.jSerialComm.*;

public class ArduinoConnection {
    SerialPort ardPort = null;
    ExperimentFlow exp = null;

    private static Properties properties = new Properties();

    static {
        try (InputStream input = BlenderConnection.class.getClassLoader().getResourceAsStream("arduinoConnection.properties")) {
            if (input == null) {
                throw new IOException("Sorry, unable to find arduinoConnection.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ArduinoConnection(ExperimentFlow experimentFlow) {
        this.exp = experimentFlow;
    }

    public void connectArduino() throws Exception {
        this.ardPort = SerialPort.getCommPort(properties.getProperty("port").toString());
        ardPort.setBaudRate(9600);
        ardPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0); // prevent blocking
    
        if (!ardPort.openPort()) {
            throw new Exception("Failed to open port");
        }

        System.out.println("arduino connected succesfully!");
    
        // Buffer to accumulate incoming data
        StringBuilder buffer = new StringBuilder();
    
        ardPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
    
            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
    
                try {
                    InputStream in = ardPort.getInputStream();
                    byte[] tempBuffer = new byte[64]; // or 128, 256...

                    int numRead = in.read(tempBuffer);
                    for (int i = 0; i < numRead; i++) {
                        char c = (char) tempBuffer[i];
                        if (c == '\n') {
                            String line = buffer.toString().trim();
                            buffer.setLength(0);
                            if (!line.isEmpty()) {
                                try {
                                    int value = Integer.parseInt(line);
                                    exp.handleArduinoNumber(value);
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid input: " + line);
                                }
                            }
                        } else {
                            buffer.append(c);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void disconnectArduino() throws Exception {
        if (ardPort != null) {
            ardPort.closePort(); 
        }
    }

    public static void sendStartSignal() {
        // TODO send start signal to arduino and start listening
    }

    public static void getTtl() {
        // TODO make a file of all the ttls and times
    }

    public static void getEncoderData() {
        // TODO write to the file
    }

    public void sendGotToReward() throws IOException {
        if(ardPort != null) {
            OutputStream out = ardPort.getOutputStream();
            out.write(1); // Send a command to Arduino
            out.flush();
        }
    }

    public static void lick() {
        // TODO write that licked
    }
}
