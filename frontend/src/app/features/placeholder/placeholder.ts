import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  imports: [RouterLink],
  selector: 'app-placeholder',
  styleUrl: './placeholder.css',
  templateUrl: './placeholder.html',
})
export class Placeholder {
  private readonly ruta = inject(ActivatedRoute);

  protected readonly titulo = (this.ruta.snapshot.data['titulo'] as string) ?? 'Pantalla';
}
