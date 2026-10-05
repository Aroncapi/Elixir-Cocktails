import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface ValorFilosofia {
  icono: string;
  titulo: string;
  texto: string;
}

@Component({
  imports: [RouterLink],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {
  protected readonly filosofia: ValorFilosofia[] = [
    {
      icono: 'liquor',
      titulo: 'Artesanal',
      texto:
        'Ingredientes seleccionados a mano, jarabes caseros y técnicas vanguardistas para crear perfiles de sabor únicos que cautivan los sentidos.',
    },
    {
      icono: 'workspace_premium',
      titulo: 'Exclusivo',
      texto:
        'Servicio personalizado y menús diseñados a medida para reflejar la esencia de su celebración, garantizando una experiencia irrepetible.',
    },
    {
      icono: 'local_bar',
      titulo: 'Profesional',
      texto:
        'Un equipo de mixólogos expertos dedicados a la excelencia, la discreción y el servicio impecable en cada detalle de su evento.',
    },
  ];
}
