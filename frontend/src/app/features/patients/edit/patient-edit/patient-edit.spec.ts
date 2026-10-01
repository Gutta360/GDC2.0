import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PatientEdit } from './patient-edit';

describe('PatientEdit', () => {
  let component: PatientEdit;
  let fixture: ComponentFixture<PatientEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientEdit],
      providers: [
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientEdit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
