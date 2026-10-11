import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Quote } from './quote';

describe('Quote', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Quote],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  it('asigna el equipo segun invitados y se actualiza al cambiar el campo', () => {
    const fixture = TestBed.createComponent(Quote);
    const comp = fixture.componentInstance as unknown as {
      guests: number;
      equipo: () => { bartenders: number; assistants: number };
    };

    expect(comp.equipo().bartenders).toBe(1);

    comp.guests = 70;
    expect(comp.equipo().bartenders).toBe(2);

    comp.guests = 120;
    expect(comp.equipo().bartenders).toBe(3);

    comp.guests = 100;
    expect(comp.equipo().assistants).toBe(1);

    comp.guests = 150;
    expect(comp.equipo().bartenders).toBe(3);
    expect(comp.equipo().assistants).toBe(2);
  });
});
