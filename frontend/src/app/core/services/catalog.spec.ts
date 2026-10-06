import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { CatalogService } from './catalog';

describe('CatalogService', () => {
  let service: CatalogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    service = TestBed.inject(CatalogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
