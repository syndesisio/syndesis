import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'syndesis-settings-root',
  templateUrl: 'settings-root.component.html',
  styleUrls: ['./settings-root.component.scss']
})
export class SettingsRootComponent {
  constructor(private route: ActivatedRoute, private router: Router) {}
}
