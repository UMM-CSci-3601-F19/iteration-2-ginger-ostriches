package umm3601.admin;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
//Todo: Maybe find something not deprecated.
import com.mongodb.util.JSON;

import static com.mongodb.client.model.Filters.eq;

public class AdminController {

  private final MongoCollection<Document> adminCollection;

  public AdminController(MongoDatabase database) {
    adminCollection = database.getCollection("Admins");
  }
  public String getAdmin(String id) {
    FindIterable<Document> jsonUsers
      = adminCollection
      .find(eq("_id", new ObjectId(id)));

    Iterator<Document> iterator = jsonUsers.iterator();
    if (iterator.hasNext()) {
      Document user = iterator.next();
      return user.toJson();
    } else {
      // We didn't find the desired user
      return null;
    }
  }

  public String getAdmins(Map<String, String[]> queryParams) {

    Document filterDoc = new Document();

    if (queryParams.containsKey("age")) {
      int targetAge = Integer.parseInt(queryParams.get("age")[0]);
      filterDoc = filterDoc.append("age", targetAge);
    }

    if (queryParams.containsKey("company")) {
      String targetContent = (queryParams.get("company")[0]);
      Document contentRegQuery = new Document();
      contentRegQuery.append("$regex", targetContent);
      contentRegQuery.append("$options", "i");
      filterDoc = filterDoc.append("company", contentRegQuery);
    }

    //FindIterable comes from mongo, Document comes from Gson
    FindIterable<Document> matchingAdmin = adminCollection.find(filterDoc);

    return serializeIterable(matchingAdmin);
  }

  private String serializeIterable(Iterable<Document> documents) {

    return StreamSupport.stream(documents.spliterator(), false)
      .map((Document d) -> d.toJson())
      .collect(Collectors.joining(", ", "[", "]"));
  }

  Boolean editInfo(String id, String bio, String phoneNumber) {
    ObjectId adminId = new ObjectId(id);
    Document filter = new Document("_id", adminId);
    Document updateFields = new Document();
    updateFields.append("bio", bio);
    updateFields.append("phoneNumber", phoneNumber);

    Document updateDoc = new Document("$set", updateFields);
    try {
      UpdateResult out = adminCollection.updateOne(filter, updateDoc);
      return out.getModifiedCount() != 0;
    } catch(MongoException e) {
      e.printStackTrace();
      return false;
    }
  }

  Boolean rateAdmin(String id, Integer totalReviewScore, Integer numReviews, Integer avgScore) {
    ObjectId objId = new ObjectId(id);
    Document filter = new Document("_id", objId);
    Document updateFields = new Document();
    updateFields.append("totalReviewScore", totalReviewScore);
    updateFields.append("numReviews", numReviews);
    updateFields.append("avgScore", avgScore);

    Document updateDoc = new Document("$set", updateFields);
    try{
      UpdateResult out = adminCollection.updateOne(filter, updateDoc);
      return out.getModifiedCount() != 0;
    }catch(MongoException e){
      e.printStackTrace();
      return false;
    }
  }

  String signup(String adminId, String email, String name, String pictureUrl){
    Document filterDoc = new Document();

    Document contentRegQuery = new Document();
    contentRegQuery.append("$regex", adminId);
    contentRegQuery.append("$options", "i");
    filterDoc = filterDoc.append("adminId", contentRegQuery);

    FindIterable<Document> matchingAdmins = adminCollection.find(filterDoc);

    if(JSON.serialize(matchingAdmins).equals("[ ]")) {
      ObjectId id = new ObjectId();

      Document newAdmin = new Document();
      newAdmin.append("_id", id);
      newAdmin.append("adminId", adminId);
      newAdmin.append("name", name);
      newAdmin.append("bio", "Nothing here yet");
      newAdmin.append("email", email);
      newAdmin.append("totalReviewScore", 0);
      newAdmin.append("numReviews", 0);

      newAdmin.append("pictureUrl", pictureUrl);
      try {
        adminCollection.insertOne(newAdmin);
        // return JSON.serialize(newAdmin);
        Document adminInfo = new Document();
        adminInfo.append("_id", matchingAdmins.first().get("_id"));
        adminInfo.append("email", matchingAdmins.first().get("email"));
        adminInfo.append("name", matchingAdmins.first().get("name"));
        adminInfo.append("pictureUrl", matchingAdmins.first().get("pictureUrl"));
        System.err.println("Successfully added new admin [_id=" + id + ", adminId=" + adminId + " email=" + email + " name=" + name + " pictureUrl " + pictureUrl + "]");
        return "New Admin added";
      }catch(MongoException e){
        e.printStackTrace();
        return "Error trying to create admin";
      }
    }else {
      return "Admin already exists";
    }
  }

  String login(String adminId, String email, String name) {

    System.out.println("Checking database for admin");
    FindIterable<Document> matchingAdmin = adminCollection.find(eq("adminId", adminId));

    System.out.println("Is this a new admin?   " + serializeIterable(matchingAdmin).equals("[]"));

    if (serializeIterable(matchingAdmin).equals("[]")) {
      ObjectId id = new ObjectId();
      Document newAdmin = new Document();

      newAdmin.append("_id", id);
      newAdmin.append("adminId", adminId);
      newAdmin.append("name", name);
      newAdmin.append("bio", "Nothing here yet");
      newAdmin.append("email", email);
      newAdmin.append("totalReviewScore", 0);
      newAdmin.append("numReviews", 0);
      try {
        adminCollection.insertOne(newAdmin);
        System.out.println("Successfully added new admin [_id: " + id + " | adminId: " + adminId + " | name: " + name + " | bio: Nothing here yet " + " | email: " + email + " | totalReviewScore: 0 | numReviews: 0");
        return "New admin successfully added";
      } catch (MongoException e) {
        e.printStackTrace();
        return "Error trying to create admin";
      }
    } else {
      System.out.println(serializeIterable(matchingAdmin));

      Document filter = new Document("adminId", adminId);
      Document getName = new Document();

      getName.append("name", name);

      Document setName = new Document("$set", getName);
      try {
        adminCollection.updateOne(filter, setName);
        System.out.println("Updating a admin [adminId: " + adminId + " | name: " + name + " was successful");
        return "Success in logging in returning admin";
      } catch (MongoException e) {
        e.printStackTrace();
        return "Error trying to log in a returning admin";
      }
    }
  }
}
