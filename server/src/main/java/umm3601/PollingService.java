package umm3601;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.internal.connection.Time;
import org.bson.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

class PollingService {

  private MongoClient mongoClient;
  private String baseApiUrl = "http://155.138.235.83:5000/";

  private final MongoCollection<Document> machineDataFromPollingAPICollection;
  private final MongoCollection<Document> roomDataFromPollingAPICollection;
  private static final String machineDataFromPollingAPIDatabaseName = "dev";
  private static final String roomDatabaseName = "dev";

  PollingService(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
    MongoDatabase machineDataFromPollingAPIDatabase = mongoClient.getDatabase(machineDataFromPollingAPIDatabaseName);
    machineDataFromPollingAPICollection = machineDataFromPollingAPIDatabase.getCollection("machineDataFromPollingAPI");
    MongoDatabase roomDataFromPollingAPIDatabase = mongoClient.getDatabase(roomDatabaseName);
    roomDataFromPollingAPICollection = roomDataFromPollingAPIDatabase.getCollection("rooms");
    this.poll();
  }

  private void poll() {
    try {
      URL url = new URL(baseApiUrl + "machines");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      int responsecode = conn.getResponseCode();
      if (responsecode != 200) {
        throw new RuntimeException("Unexpected HttpResponseCode when looking for 200: " + responsecode);
      } else {
        //the response code suggests all is well and we need to get the string from the stream
        String inline = "";
        Scanner sc = new Scanner(url.openStream());
        while (sc.hasNext()) {
          String item = sc.nextLine();
          inline += item;
        }
        System.out.println("\nJSON data in string format");
        System.out.println(inline);
        sc.close();
        //use that string to update machine data
        updateAllMachineData(inline);
      }

    } catch (MalformedURLException exception) {
      System.out.println("The url: " + baseApiUrl + "machines" + " was malformed.");
      exception.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
    try {
      URL url = new URL(baseApiUrl + "rooms");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      int responsecode = conn.getResponseCode();
      if (responsecode != 200) {
        throw new RuntimeException("Unexpected HttpResponseCode when looking for 200: " + responsecode);
      } else {
        //the response code suggests all is well and we need to get the string from the stream
        String inline = "";
        Scanner sc = new Scanner(url.openStream());
        while (sc.hasNext()) {
          String item = sc.nextLine();
          inline += item;
        }
        System.out.println("\nJSON data in string format");
        System.out.println(inline);
        sc.close();
        //use that string to update room data
        updateAllRoomData(inline);
      }

    } catch (MalformedURLException exception) {
      System.out.println("The url: " + baseApiUrl + "rooms" + " was malformed.");
      exception.printStackTrace();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }

  }

  private void updateAllMachineData(String responseData) {

    BsonArray theArray = BsonArray.parse(responseData);
    Iterator<BsonValue> bsonValues = theArray.iterator();
    if (machineDataFromPollingAPICollection.count() == 0) {

    System.out.println("THE ARRAY OF DATA IS THIS MANY ITEMS:" + theArray.size());

      while (bsonValues.hasNext()) {
        BsonDocument thisDocument = bsonValues.next().asDocument();
        System.out.println(thisDocument);

        Document d = new Document();
        for (Map.Entry<String, BsonValue> e : thisDocument.entrySet()) {
          d.append(e.getKey(), e.getValue());
        }
        //It would probably be better to use the timestamp of the database, but this might make it easier to index
        d.append("timestamp", "" + Time.nanoTime());
        machineDataFromPollingAPICollection.insertOne(d);
      }
      return;
    }
  }
  private void updateAllRoomData(String responseData) {

    BsonArray theArray = BsonArray.parse(responseData);
    Iterator<BsonValue> bsonValues = theArray.iterator();
    if (roomDataFromPollingAPICollection.count() == 0) {
    System.out.println("THE ARRAY OF DATA IS THIS MANY ITEMS:" + theArray.size());
      while (bsonValues.hasNext()) {
        BsonDocument thisDocument = bsonValues.next().asDocument();
        System.out.println(thisDocument);

        Document d = new Document();
        for (Map.Entry<String, BsonValue> e : thisDocument.entrySet()) {
          d.append(e.getKey(), e.getValue());
        }
        //It would probably be better to use the timestamp of the database, but this might make it easier to index
        d.append("timestamp", "" + Time.nanoTime());
        roomDataFromPollingAPICollection.insertOne(d);
      }
      return;
    }
  }
}
