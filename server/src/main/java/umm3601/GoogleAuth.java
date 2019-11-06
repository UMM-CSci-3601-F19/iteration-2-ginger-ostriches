package umm3601;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import spark.Request;

import java.util.Collections;
import java.util.Iterator;

public class GoogleAuth {
  private static final String CLIENT_ID = "824632109956-7oc5g0ereu6rqhqoikqsg2nu92g48v4e.apps.googleadmincontent.com";

  private static final NetHttpTransport transport = new NetHttpTransport();

  private static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JacksonFactory.getDefaultInstance())
    // Specify the CLIENT_ID of the app that accesses the backend:
    .setAudience(Collections.singletonList(CLIENT_ID))
    // Or, if multiple clients access the backend:
    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
    .build();

  private final MongoCollection<Document> adminCollection;

  public GoogleAuth(MongoDatabase database) {
    this.adminCollection = database.getCollection("Admins");
  }

  public GoogleIdToken auth(Request req){
    return auth(Document.parse(req.body()));
  }
  public GoogleIdToken auth(Document body){
    return auth(body.getString("idtoken"));
  }
  public GoogleIdToken auth(String token){
    try {
      System.out.println(token);
      return verifier.verify(token);
    } catch (Exception e) {
      //Should return 401: Unauthorized
      System.err.println("Invalid ID token");
      e.printStackTrace();
      return null;
    }
  }

  public String getEmail(String token){
    return auth(token).getPayload().getEmail();
  }

  public String getName(Request req){
    return getName(Document.parse(req.body()));
  }
  public String getName(Document body) { return getName(body.getString("idtoken"));}
  public String getName(String token){
    return (String) auth(token).getPayload().get("name");
  }

  public String getAdminId(String token){
    return (String) auth(token).getPayload().getSubject();
  }
  public String getPicture(String token){
    return (String) auth(token).getPayload().get("picture");
  }

  public String getAdminMongoId(GoogleIdToken token){
    return getAdminMongoId(token.getPayload().getSubject());
  }

  public String getAdminMongoId(String googleSubjectId) {
    Document filterDoc = new Document("adminId", googleSubjectId);
    FindIterable<Document> matchingAdmin = adminCollection.find(filterDoc);
    Iterator<Document> iterator = matchingAdmin.iterator();
    if (iterator.hasNext()) {
      Document admin = iterator.next();
      String adminMongoId = admin.getObjectId("_id").toHexString();
      System.out.println("Got admin's mongo ID: " + adminMongoId);
      return adminMongoId;
    } else {
      return null;
    }
  }

}
