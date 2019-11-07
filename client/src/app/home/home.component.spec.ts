import {TestBed, ComponentFixture, async} from '@angular/core/testing';
import {HomeComponent} from './home.component';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {CustomModule} from '../custom.module';
import {HomeService} from './home.service';
import {Machine} from './machine';
import {Room} from './room';
import {Observable} from 'rxjs';
import {AuthService} from '../auth.service';

describe('Home page', () => {

  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let de: DebugElement;
  let df: DebugElement;
  let dg: DebugElement;
  let dh: DebugElement;
  let el: HTMLElement;
  let fl: HTMLElement;
  let gl: HTMLElement;
  let hl: HTMLElement;

  let homeServiceStub: {
    getRooms: () => Observable<Room[]>;
    getMachines: () => Observable<Machine[]>;
    updateRunningStatus;
  };
  let authServiceStub: {
    getAdminId: () => String,
    getAdminName: () => String,
    isSignedIn: () => boolean,
    loadClient: () => null;
  };

  // @ts-ignore
  beforeEach(() => {
    homeServiceStub = {
      getMachines: () => Observable.of([{
        id: 'id_1',
        name: 'Bob',
        running: false,
        status: 'normal',
        room_id: 'room_a',
        type: 'washer',
        position: {
          x: 0,
          y: 0
        },

        remainingTime: -1,
        vacantTime: 10,
      }, {
        id: 'string',
        name: 'Geroge',
        running: false,
        status: 'normal',
        room_id: 'room_b',
        type: 'dryer',
        position: {
          x: 0,
          y: 0
        },

        remainingTime: 10,
        vacantTime: -1,
      }]),
      getRooms: () => Observable.of([{
        id: 'room_a',
        name: 'A',

        numberOfAllMachines: null,
        numberOfAvailableMachines: null,
      }, {
        id: 'room_b',
        name: 'B',

        numberOfAllMachines: null,
        numberOfAvailableMachines: null,
      }, ]),
      updateRunningStatus: () => null,
    };
    authServiceStub = {
      getAdminId: () => 'MI6007',
      getAdminName: () => 'James Bond',
      isSignedIn: () => true,
      loadClient: () => null,
    };


    TestBed.configureTestingModule({
      imports: [CustomModule],
      declarations: [HomeComponent], // declare the test component
      providers: [{provide: HomeService, useValue: homeServiceStub},
        {provide: AuthService, useValue: authServiceStub }]
    });

    fixture = TestBed.createComponent(HomeComponent);

    component = fixture.componentInstance; // BannerComponent test instance

    // query for the link (<a> tag) by CSS element selector
    de = fixture.debugElement.query(By.css('#home-rooms-card'));
    df = fixture.debugElement.query(By.css('#home-machines-card-washer'));
    dg = fixture.debugElement.query(By.css('#home-machines-card-dryer'));
    dh = fixture.debugElement.query(By.css('#home-machines-card-broken'));
    el = de.nativeElement;
    fl = df.nativeElement;
    gl = dg.nativeElement;
    hl = dh.nativeElement;
  });

  it('displays a text of rooms', () => {
    fixture.detectChanges();
    expect(el.textContent).toContain('Please select a laundry room here');
  });

  it('displays a text of washers', () => {
    fixture.detectChanges();
    expect(fl.textContent).toContain('Washers available within all rooms');
  });

  it('displays a text of dryers', () => {
    fixture.detectChanges();
    expect(gl.textContent).toContain('Dryers available within all rooms');
  });

  it('displays a text of broken machines', () => {
    fixture.detectChanges();
    expect(hl.textContent).toContain('Unavailable machines within all rooms');
  });

  it('update room info when a new room is selected', () => {
    const machines: Observable<Machine[]> = homeServiceStub.getMachines();
    machines.subscribe(
      // tslint:disable-next-line:no-shadowed-variable
      machines => {
        component.machines = machines;
      });
    component.updateRoom('room_b', 'B');
    expect(component.roomId).toBe('room_b');
    expect(component.roomName).toBe('B');
  });

  it('update machines with a new room id', () => {
    const machines: Observable<Machine[]> = homeServiceStub.getMachines();
    machines.subscribe(
      // tslint:disable-next-line:no-shadowed-variable
      machines => {
        component.machines = machines;
      });
    component.updateRoom('room_a', 'A');
    expect(component.filteredMachines.length).toBe(1);
    expect(component.filteredMachines[0].id).toBe('id_1');
  });

  it('load all the machines', () => {
    const machines: Observable<Machine[]> = homeServiceStub.getMachines();
    machines.subscribe(
      // tslint:disable-next-line:no-shadowed-variable
      machines => {
        component.machines = machines;
      });
    expect(component.machines.length).toBe(2);
  });

  it('load all the rooms', () => {
    const rooms: Observable<Room[]> = homeServiceStub.getRooms();
    rooms.subscribe(
      // tslint:disable-next-line:no-shadowed-variable
      rooms => {
        component.rooms = rooms;
      });
    expect(component.rooms.length).toBe(2);
  });

  it('should load and update the time remaining', () => {
    let spy = spyOn(component, 'ngOnInit');
    component.ngOnInit();
    expect(spy).toHaveBeenCalled();
    spy = spyOn(component, 'updateTime');
    component.updateTime();
    expect(spy).toHaveBeenCalled();
  });
});
