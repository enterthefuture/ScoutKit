
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;

public class ScoutServer {

    public static void main(String[] args) {
        int port = 11963;
        HashMap<Integer,HashMap<String,Integer>> stats
                = new HashMap<Integer,HashMap<String,Integer>>();
        
        if (args.length == 0) {
            System.err.println("No port specified. Default: 11963");
        } else {
            port = Integer.parseInt(args[0]);
        }
        HashMap<String,Integer> teamstats = new HashMap<String,Integer>();
        while (true) {
            try {
                ServerSocket server = new ServerSocket(port);
                Socket client = server.accept();
                
                Message event = (Message) (new ObjectInputStream(client.getInputStream())).readObject();
                System.out.println("\n"+event);
                
                if(stats.containsKey(event.teamNo)) {
                    teamstats = stats.get(event.teamNo);
                } else {
                    teamstats.put("low", 0);
                    teamstats.put("high", 0);
                    teamstats.put("throw", 0);
                    teamstats.put("catch", 0);
                }
                
                int oldstat = teamstats.get(event.attribute);
                teamstats.put(event.attribute, oldstat+event.value);
                stats.put(event.teamNo, teamstats);
                
                System.out.println(stats);
                
                client.close();
                server.close();
            } catch (Exception e) {
                System.err.println(e);
                System.exit(2);
            }
        }
    }
}
