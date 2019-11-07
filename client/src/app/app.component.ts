import {Component} from '@angular/core';
import {AuthService} from './auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'Web App';
  public profileID = localStorage.getItem('userID');
  public auth: AuthService;

  constructor(private authService: AuthService) {}
}
