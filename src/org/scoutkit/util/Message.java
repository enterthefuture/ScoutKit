package org.scoutkit.util;

import java.io.*;
import java.text.*;
import java.util.*;

public class Message implements Serializable {
	public String scoutID;
	public int teamNo;
	public int match;
	public String stat;
	public String value;
	public long timeStamp;

	public Message(String scoutID, int teamNo, int match , String stat, String value) {
		this.scoutID=scoutID;
		this.teamNo=teamNo;
    this.match=match;
		this.stat=stat;
		this.value=value;

		this.timeStamp = System.currentTimeMillis();
	}

	public String toString() { return "(" + scoutID + "-" + timeStamp + ") " + teamNo +":"+match+":"+ stat + ":" + value; }
}
