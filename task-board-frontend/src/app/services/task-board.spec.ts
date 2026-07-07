import { TestBed } from '@angular/core/testing';

import { TaskBoard } from './task-board';

describe('TaskBoard', () => {
  let service: TaskBoard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaskBoard);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
