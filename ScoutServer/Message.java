import java.io.*;
import java.text.*;
import java.util.*;

public class Message implements Serializable {
	public String scoutID;
	public int teamNo;
        public int match;
	public String attribute;
	public int value;
	public long timeStamp;
	
	public Message(String scoutID, int teamNo, int match , String attribute, int value) {
		this.scoutID=scoutID;
		this.teamNo=teamNo;
                this.match = match;
		this.attribute=attribute;
		this.value=value;
		this.timeStamp = System.currentTimeMillis();
	}
	
	public String toString() { return "(" + scoutID + "-" + timeStamp + ") " + teamNo +":"+match+":"+ attribute + ":" + value; }
}