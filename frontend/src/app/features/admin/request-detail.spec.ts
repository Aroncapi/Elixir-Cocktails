import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RequestDetail } from './request-detail';

describe('RequestDetail', () => {
  let component: RequestDetail;
  let fixture: ComponentFixture<RequestDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestDetail],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(RequestDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
