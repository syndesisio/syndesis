import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'syndesis-customizations',
  templateUrl: 'customizations.component.html',
  styleUrls: ['./customizations.component.scss']
})
export class CustomizationsComponent {
  constructor(private route: ActivatedRoute, private router: Router) {}
}
