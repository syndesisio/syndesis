import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'syndesis-connections-cancel',
  template: ''
})
export class ConnectionsCancelComponent implements OnInit {
  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.router.navigate(['../..'], { relativeTo: this.route });
  }
}
