import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController, TestRequest } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RequestsAdmin } from './requests-admin';

describe('RequestsAdmin', () => {
  let component: RequestsAdmin;
  let fixture: ComponentFixture<RequestsAdmin>;
  let httpMock: HttpTestingController;
  let pagina: TestRequest;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestsAdmin],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(RequestsAdmin);
    component = fixture.componentInstance;
    fixture.detectChanges();

    pagina = httpMock.expectOne(
      (req) => req.url === '/api/requests' && req.params.has('page'),
    );
    pagina.flush({ items: [], total: 0, page: 0, size: 10, totalPages: 1 });

    httpMock
      .expectOne((req) => req.url === '/api/requests/resumen')
      .flush({ solicitudes: 0, ingresosEstimados: 0, proximosEventos: 0, pendientes: 0, confirmadas: 0, canceladas: 0 });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should request page 0 with size 10 on init', () => {
    expect(pagina.request.method).toBe('GET');
    expect(pagina.request.params.get('page')).toBe('0');
    expect(pagina.request.params.get('size')).toBe('10');
  });
});
