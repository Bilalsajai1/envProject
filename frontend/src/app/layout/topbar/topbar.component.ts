import {Component, EventEmitter, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-topbar',
  standalone: false,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
})
export class TopbarComponent implements OnInit {

  isSidebarCollapsed = false
  @Output() sidebarToggle = new EventEmitter<boolean>();

  /*constructor(private authenticationService: AuthenticationService) {
  }
*/
  ngOnInit() {


  }

  /*logout() {
    this.authenticationService.logout();
  }*/

  onToggle() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed
    this.sidebarToggle.emit(this.isSidebarCollapsed);
  }
}

