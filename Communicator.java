/*
1.Searching for serial ports
2.Connecting to the serial port
3.Starting the input output streams
4.Adding an event listener to listen for incoming data
5.Disconnecting from the serial port
6.Sending Data
7.Receiving Data

*/


import java.util.*;
import gnu.io.*;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class Communicator implements SerialPortEventListener
{
	//passed from main GUI
    //GUI window = null;

    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";


    //search for all the serial ports
    //pre style="font-size: 11px;": none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
            System.out.println(curPort.getName());
            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                if ("/dev/ttyUSB0".equals(curPort.getName())) {
                    selectedPortIdentifier=curPort;
                    connect();
                }
            	
                // window.cboxPorts.addItem(curPort.getName());
                // portMap.put(curPort.getName(), curPort);
            }
        }
    }

    public static void main(String[] args) {
    	Communicator communicator = new Communicator();
        communicator.searchForPorts();
        //communicator.connect();
        System.out.println(communicator.initBaudRate());
        System.out.println(communicator.initIOStream());
        communicator.initListener();
        //communicator.serialEvent();
        String message = "hello";
        System.out.println(Arrays.toString(message.getBytes()));
        try{
            while(true){
                communicator.output.write(message.getBytes());
                Thread.sleep(1000);
            }
        }catch(Exception e){
            System.out.println("send exception");
        }

    }


    //connect to the selected port in the combo box
    //pre style="font-size: 11px;": ports are already found by using the searchForPorts
    //method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect()
    {
        //String selectedPort = (String)window.cboxPorts.getSelectedItem();
        //selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            //commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)selectedPortIdentifier.open("Tiger",TIMEOUT);
            //serialPort = (SerialPort)commPort;

            //for controlling GUI elements
            //setConnected(true);

            //logging
            logText = selectedPortIdentifier.getName() + " opened successfully.";
            System.out.println(logText);
            //window.txtLog.setForeground(Color.black);
            //window.txtLog.append(logText + "n");

            //CODE ON SETTING BAUD RATE ETC OMITTED
            //XBEE PAIR ASSUMED TO HAVE SAME SETTINGS ALREADY

            //enables the controls on the GUI if a successful connection is made
            //window.keybindingController.toggleControls();
        }
        catch (PortInUseException e)
        {
            logText = selectedPortIdentifier + " is in use. (" + e.toString() + ")";
            System.out.println(logText);

            //window.txtLog.setForeground(Color.RED);
            //window.txtLog.append(logText + "n");
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPortIdentifier + "(" + e.toString() + ")";
            System.out.println(logText);
            //window.txtLog.append(logText + "n");
            //window.txtLog.setForeground(Color.RED);
        }
    }

    public boolean initBaudRate()
    {
        boolean flag=false;
        try{
            serialPort.setSerialPortParams(
                    9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
                );
            flag = true;
            return flag;
        }catch(UnsupportedCommOperationException e){
            System.out.println("init Baud Rate fialed");
            return flag;
        }
    }
    //open the input and output streams
    //pre style="font-size: 11px;": an open port
    //post: initialized input and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whether opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            //writeData(0, 0);

            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            //window.txtLog.setForeground(Color.red);
            //window.txtLog.append(logText + "n");
            return successful;
        }
    }


    //starts the event listener that knows whenever data is available to be read
    //pre style="font-size: 11px;": an open serial port
    //post: an event listener for the serial port that knows when data is received
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            //window.txtLog.setForeground(Color.red);
            //window.txtLog.append(logText + "n");
        }
    }

/*
    //disconnect the serial port
    //pre style="font-size: 11px;": an open serial port
    //post: closed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            writeData(0, 0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            window.keybindingController.toggleControls();

            logText = "Disconnected.";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "n");
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName()
                              + "(" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "n");
        }
    }



    //what happens when data is received
    //pre style="font-size: 11px;": serial event is triggered
    //post: processing on the data it reads
    */
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                byte singleData = (byte)input.read();
                System.out.print(singleData+" ");
                if (singleData != NEW_LINE_ASCII)
                {
                    logText = new String(new byte[] {singleData});
                    //System.out.print(logText);
                    //window.txtLog.append(logText);
                }
                else
                {
                    //window.txtLog.append("n");
                }
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                System.out.println(logText);
                //window.txtLog.setForeground(Color.red);
                //window.txtLog.append(logText + "n");
            }
        }
    }

    public String[] toHex(byte[] b)
    {
        String[] hex=null;
        for (int i = 0;i < b.length ; i++) {
            //System.out.print(Integer.toHexString(b[i])+" ");
            hex[i] = Integer.toHexString(b[i]);
        }
        return hex;
        //System.out.println();
    }
}
/*
    //method that can be called to send data
    //pre style="font-size: 11px;": open serial port
    //post: data sent to the other device
    public void writeData(int leftThrottle, int rightThrottle)
    {
        try
        {
            output.write(leftThrottle);
            output.flush();
            //this is a delimiter for the data
            output.write(DASH_ASCII);
            output.flush();

            output.write(rightThrottle);
            output.flush();
            //will be read as a byte so it is a space key
            output.write(SPACE_ASCII);
            output.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";
            window.txtLog.setForeground(Color.red);
            window.txtLog.append(logText + "n");
        }
    }


    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {
        communicator.connect();
        if (communicator.getConnected() == true)
        {
            if (communicator.initIOStream() == true)
            {
                communicator.initListener();
            }
        }
    }


}
*/