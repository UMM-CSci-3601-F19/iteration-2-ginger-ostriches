import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment} from '../environments/environment';
import 'rxjs/add/observable/of';

declare let gapi: any;

@Injectable()
export class AppService {
  constructor(private http: HttpClient) {
  }

  googleAuth;

  initClient() {
    gapi.client.init({
      'clientId': '824632109956-7oc5g0ereu6rqhqoikqsg2nu92g48v4e.apps.googleusercontent.com',
      'scope': 'profile email'
    });
  }


  sendAuthCode(code: string): void {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
    };
  }

  /*  this.http.post(environment.API_URL + 'login', {code: code}, httpOptions).subscribe(onSuccess => {
    console.log('Code sent to server');
    console.log(onSuccess);
    localStorage.setItem('_id', onSuccess['_id']);
    localStorage.setItem('userId', onSuccess['userId']);
    localStorage.setItem('oid', onSuccess['_id']['$oid']);
    localStorage.setItem('email', onSuccess['email']);
    localStorage.setItem('userFullName', onSuccess['fullName']);
    localStorage.setItem('userLastName', onSuccess['lastName']);
    localStorage.setItem('userFirstName', onSuccess['firstName']);
    localStorage.setItem('pictureUrl', onSuccess['pictureUrl']);
    window.location.reload();

    console.log(localStorage.getItem('_id'));
    console.log(localStorage.getItem('userId'));
    console.log(localStorage.getItem('oid'));
    console.log(localStorage.getItem('email'));
    console.log(localStorage.getItem('userFullName'));
    console.log(localStorage.getItem('userLastName'));
    console.log(localStorage.getItem('userFirstName'));
    console.log(localStorage.getItem('pictureUrl'));

  }, onFail => {
    console.log('ERROR: Code couldn't be sent to the server'');
  });*/

  signIn() {
    this.googleAuth = gapi.auth2.getAuthInstance();
    console.log(' This is google Auth ' + this.googleAuth);
    this.googleAuth.grantOfflineAccess().then((resp) => {
      localStorage.setItem('isSignedIn', 'true');
      this.sendAuthCode(resp.code);
    });
  }

  public isSignedIn(): boolean {
    status = localStorage.getItem('isSignedIn');
    if (status === 'true') {
      return true;
    } else {
      return false;
    }
  }
}
