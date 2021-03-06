package cs425.mp4.crane.Apps;

import cs425.mp4.crane.Constants;
import cs425.mp4.crane.CraneSubmitter;
import cs425.mp4.crane.Exceptions.InvalidIDException;
import cs425.mp4.crane.Topology.Bolt;
import cs425.mp4.crane.Topology.Spout;
import cs425.mp4.crane.Topology.TopologyBuilder;

import java.util.HashMap;
import java.util.Random;

/**
 * Example topology in crane
 */
public class CraneExample {
    private static class RandomWord implements Spout {

        public void open() {

        }

        public HashMap<String, String> nextTuple() {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String [] words= {"parijat","ashu","jayant","kartik","arka","jacob"};
            Random rn=new Random();
            HashMap<String,String> emit=new HashMap<String, String>();
            emit.put("word",words[rn.nextInt(words.length)]);
            return emit;
        }
    }

    private static class IdentityBolt implements Bolt {

        public void open() {

        }

        public HashMap<String, String> execute(HashMap<String, String> in) {
            return in;
        }
    }
    public static void main(String [] args) {
        TopologyBuilder tb=new TopologyBuilder();
        try {
            tb.setSpout("randomWord", new RandomWord());
            tb.setBoltShuffleGrouping("shuffleIdentity", new IdentityBolt(), 2, "randomWord");
            tb.setBoltFieldsGrouping("fieldsIdentity", new IdentityBolt(), 2, "shuffleIdentity", "word");
            CraneSubmitter cs=new CraneSubmitter(args[0], Constants.NIMBUS_PORT);
            cs.submitTopology(tb.topology);
            Thread.sleep(1000);
        } catch (InvalidIDException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
