As of [v1.5 Release](https://github.com/wastevensv/ScoutKit/releases/tag/v1.5) it is now possible to configure ScoutKit before compiling.


#Dependencies#

* JDK and JRE
* [Apache Ant](https://ant.apache.org/)
* A text editor
* Access to a command line

#Configuration Instructions#

1. Install dependencies
2. Edit ScoutKit.properties (in src/ folder)
3. Open command prompt and enter "ant" while in the ScoutKit folder (the folder with the build.xml file).

#Configuration Options#

* Keys
  * **critAKey** - single word for event A (ex. high), only used on the backend <br/>
     ...<br/>
  * **critDKey** - single word for event D

* Names
  * **critAText** - name for event A (ex. High Goal), used on ScoutBox buttons<br/>
     ...<br/>
  * **critDText** - name for event D

* System
  * **port** - a numerical port number to use for server to client communication.
  * **statsSQL** - string of derby-compatible SQL code used to calculate statistics
     * default code
       ```SQL
       SELECT team, COUNT(MATCHNO) AS ENTRIES, AVG(CAST(A AS FLOAT)) AS AVG_HIGH, AVG(CAST(B AS FLOAT)) AS AVG_LOW, AVG(CAST(C AS FLOAT)) AS AVG_CATCH, AVG(CAST(D AS FLOAT)) AS AVG_THROW FROM matches GROUP BY team ```
