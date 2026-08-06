export interface TaskBoardDto {
  id: string;
  name: string;
  isPublic: boolean;
  tasks: TaskDto[];
  canChangeVisibility: false;
  canEdit: true;
}

export interface CreateTaskBoardCommandDto {
  name: null;
}

export interface TaskDto {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
}

export interface UpdateTaskCommandDto {
  newTitle: string;
  newDescription: string;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
