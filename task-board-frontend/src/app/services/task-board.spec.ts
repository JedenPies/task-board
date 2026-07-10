import { TestBed } from '@angular/core/testing';

import { TaskBoardService } from './task-board';

describe('TaskBoard', () => {
  let service: TaskBoardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TaskBoardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
