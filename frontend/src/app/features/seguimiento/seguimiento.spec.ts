import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Seguimiento } from './seguimiento';

describe('Seguimiento', () => {
  let component: Seguimiento;
  let fixture: ComponentFixture<Seguimiento>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Seguimiento],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Seguimiento);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
