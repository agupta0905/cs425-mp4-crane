package cs425.mp4.crane;

import cs425.mp4.crane.Topology.Bolt;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * Task thread that hosts bolt logic. Receives input tuple,
 * processes it using bolt logic, forwards to next bolts in
 * topology.
 */
public class WorkerThread extends Thread {
    private final int BYTE_LEN=10000;
    private final Bolt bolt;
    private final Forwarder fd;
    private final Worker wk;
    private final DatagramSocket socket;
    private final String taskID;
    public WorkerThread(String taskID, Bolt b, Forwarder fd, int port, Worker wk) throws IOException {
        bolt=b;
        this.fd=fd;
        this.wk=wk;
        socket=new DatagramSocket(port);
        bolt.open();
        this.taskID=taskID;
    }

    @Override
    public void run() {
        byte [] receiveData=new byte[BYTE_LEN];
        while(true) {
            try {
//                System.err.println("[BOLT_TASK] Waiting for next packet "+taskID);
                DatagramPacket pack=new DatagramPacket(receiveData,receiveData.length);
                socket.receive(pack);
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(receiveData));
                CraneData in = (CraneData) is.readObject();
                is.close();

//                System.err.println("[BOLT_TASK] Received at " + taskID + " tuple_id : "+in.tupleID);
                in.val = bolt.execute(in.val);
                HashMap<String,TaskAddress> taskAddress=wk.getTask2AddressMap();
//                System.err.println("[BOLT_TASK] " + taskID + " Emit : " + in.tupleID + " : " + in.val);
                if (in.val==null || fd.isLeaf()) {
                    sendAck(in, taskAddress.get(fd.spoutID).hostname, taskAddress.get(fd.spoutID).port, fd.numAcks);
                } else {
                    for (String taskID : fd.getForwardTaskIDs(in.val)) {
                        forwardResult(in, taskAddress.get(taskID).hostname, taskAddress.get(taskID).port);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send ack to acker thread if this is the last bolt in topology
     * or if the input tuple is filtered out by the bolt
     * @param in Input data
     * @param hostname acker hostname
     * @param port acker port
     * @param numAcks weight of ack sent by this bolt
     */
    private void sendAck(CraneData in, String hostname, int port, int numAcks) {
        try {
            ByteArrayOutputStream bo=new ByteArrayOutputStream(BYTE_LEN);
            ObjectOutputStream os=new ObjectOutputStream(bo);
            os.writeObject(in);
            os.writeObject(numAcks);
            os.flush();

            byte [] sendBytes=bo.toByteArray();
            DatagramPacket dp=new DatagramPacket(sendBytes,sendBytes.length, InetAddress.getByName(hostname),port);
            DatagramSocket dSock=new DatagramSocket();
            dSock.send(dp);

            os.close();
            dSock.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send result to subsequent bolts
     * @param in
     * @param hostname
     * @param port
     */
    private void forwardResult(CraneData in, String hostname, int port) {
        try {
            ByteArrayOutputStream bo=new ByteArrayOutputStream(BYTE_LEN);
            ObjectOutputStream os=new ObjectOutputStream(bo);
            os.writeObject(in);
            os.flush();
            byte [] sendBytes=bo.toByteArray();
            DatagramPacket dp=new DatagramPacket(sendBytes,sendBytes.length, InetAddress.getByName(hostname),port);
            DatagramSocket dSock=new DatagramSocket();
            dSock.send(dp);
            os.close();
            dSock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
