import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Servicio {
  titulo: string;
  texto: string;
  imagen: string;
}

@Component({
  imports: [RouterLink],
  selector: 'app-services',
  styleUrl: './services.css',
  templateUrl: './services.html',
})
export class Services {
  protected readonly servicios: Servicio[] = [
    {
      titulo: 'Barras Libres Premium',
      texto:
        'Diseñadas para bodas y galas exclusivas. Una selección curada de destilados de alta gama, coctelería de autor y un servicio ininterrumpido que define la elegancia del evento.',
      imagen:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuA-CgX_nmXRFbQVKy68-Y-n6PCMe3hT_AGle6qKOw9n62MH5R7EG9qZoFj7dkVmlP_rjeYQyivrlXdGSNyW2q_OUwn9YzFIQ36dCSEOLvQQEn6LSYImX8YxVvBZoQwIzwKVgGO1BNwsVs2Y5XOSgUUYh0wmHbf96-MWU_Kfk_zNQvQA753xbgN1S-viJX-8N56jCDfLc-VIxpCrT3kGCV_8Q-LHH5HPhZ6dcNtd_wXDAiyFa2nq7FGe',
    },
    {
      titulo: 'Mixología Corporativa',
      texto:
        'Activaciones de marca y eventos empresariales. Coctelería personalizada que refleja la identidad visual y los valores de su empresa, ejecutada con precisión milimétrica.',
      imagen:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuAIsxaAteJuSzmWZBx13AbFsJoNg08ZF_o9XJ-Z-DoSI5b096dg6BWs9-2hGcTjRLrfnxSOk-fXvNbviEkP3anTQPlG-IpGiBXYDFqNQPvoESxIwRcSK88apalvFUxteE4qAqzSwfBikB9hsHs7KBRfcy9X6VqqdHQZgmncGt-1Rwk-HhYPHnokX91_xUP6UqKg9cSuWf8-cM9rH1I0vQuyXsyVg7fXSGwK50RDp5707hr_JWw0NH5d',
    },
    {
      titulo: 'Masterclasses Privadas',
      texto:
        'Talleres interactivos diseñados para grupos íntimos. Una inmersión profunda en la historia, las técnicas y la cata de destilados selectos, guiada por nuestros expertos mixólogos.',
      imagen:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCBjaJLLnEc9_oNDTEOzdgTpDG--R4-7OZ50BETA7CGKnI8_Diym2-R5YY8xwAgYztwqD25YsYhS4h5K7xgkXZ65Ojb_XJNjG5fAbi5AKQS72l62fMjcMm0DFwiYfmouDIH8C7VQ7YQ8pjVRvOyNPwXjpOUX3ctPW2KwCpsdee65KMmBGGwIbh4hg8VzmNRQ2zb4xSmmBjIlTPgfsnQcnIbgddJuOZ0ocVvSqt6jTfmEi7xVtbRI9Wp',
    },
    {
      titulo: 'Catering de Coctelería',
      texto:
        'Menús a medida para ocasiones especiales. Integración perfecta de perfiles de sabor únicos que complementan la propuesta gastronómica de su evento con absoluta sofisticación.',
      imagen:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuBlkS-_9Mz0zthCDRTflnGWz6Bt5alMqNJuvwbv9woyVnZxJMdTb79pn4y7_LXIw6dougLMK_4qSWGwcRlBqe03xWVlqzkJkJW8UqVUFIJKFEjRiBeqDsYInWiX_-yFKLZVcjkJih5ycTYmjieke6Yo9jxiAnPvLI9Dk2hM4SdmOEKJiWPTxiq0uahBQ0tPgZ8_8PViG4FrnL3C9R0CIdWhaa0SlXIeiwTkShOwvbHxsAwvpHIlyyAJ',
    },
  ];
}
