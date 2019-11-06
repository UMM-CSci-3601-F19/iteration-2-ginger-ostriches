package umm3601.admin;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.bson.Document;
import spark.Request;
import spark.Response;
import umm3601.GoogleAuth;
import umm3601.Server;
import umm3601.admin.AdminController;

import java.util.Collections;
import java.util.List;

public class AdminRequestHandler {
  private final AdminController adminController;
  private final GoogleAuth gauth;


  private static final String CLIENT_ID = "375549452265-kpv6ds6lpfc0ibasgeqcgq1r6t6t6sth.apps.googleadmincontent.com";

  private static final String CLIENT_SECRET_FILE = "../secret.json";

  private static NetHttpTransport transport = new NetHttpTransport();

  public AdminRequestHandler(AdminController adminController, GoogleAuth gauth) {
    this.adminController = adminController;
    this.gauth = gauth;
  }

  public String getAdminJSON(Request req, Response res) {
    res.type("application/json");
    String id = req.params("id");
    String admin;
    try {
      admin = adminController.getAdmin(id);
    } catch (IllegalArgumentException e) {
      // This is thrown if the ID doesn't have the appropriate
      // form for a Mongo Object ID.
      // https://docs.mongodb.com/manual/reference/method/ObjectId/
      res.status(400);
      res.body("The requested admin ID, " + id + ", wasn't a legal Mongo Object ID.\n" +
        "See 'https://docs.mongodb.com/manual/reference/method/ObjectId/' for more info.");
      return "";
    }
    if (admin != null) {
      return admin;
    } else {
      res.status(404);
      res.body("The requested admin with id " + id + " was not found");
      return "";
    }
  }

  public String getAdmins(Request req, Response res) {
    res.type("application/json");
    return adminController.getAdmins(req.queryMap().toMap());
  }



  public String login(Request req, Response res) {
    res.type("application/json");

    Document body = Document.parse(req.body());
    String token = body.getString("idtoken"); //key formerly 'code'
    GoogleIdToken idToken = gauth.auth(token);
    if (idToken != null) {
      GoogleIdToken.Payload payload = idToken.getPayload();
      String adminId = payload.getSubject();
      String email = payload.getEmail();
      String name = (String) payload.get("name");
      return adminController.login(adminId, email, name);
    } else {
      return null;
    }
  }
  public String signup(Request req, Response res) {
    res.type("application/json");

    Document body = Document.parse(req.body());
    GoogleIdToken idToken = gauth.auth(body);
    if (idToken != null) {
      GoogleIdToken.Payload payload = idToken.getPayload();
      String adminId = payload.getSubject();
      String email = payload.getEmail();
      String name = (String) payload.get("name");
      String pictureUrl = (String) payload.get("picture");
      return adminController.signup(adminId, email, name, pictureUrl);
    }else{
      return null;
    }
  }
}
